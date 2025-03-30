package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dtos.request.*;
import com.toanyone.delivery.application.dtos.response.*;
import com.toanyone.delivery.application.exception.DeliveryException;
import com.toanyone.delivery.application.exception.DeliveryManagerException;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.common.utils.SingleResponse;
import com.toanyone.delivery.common.utils.UserContext;
import com.toanyone.delivery.domain.Delivery;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryManager.DeliveryManagerType;
import com.toanyone.delivery.domain.DeliveryRoad;
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import com.toanyone.delivery.domain.repository.CustomDeliveryRepository;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import com.toanyone.delivery.domain.repository.DeliveryRepository;
import com.toanyone.delivery.infrastructure.client.AiClient;
import com.toanyone.delivery.infrastructure.client.HubClient;
import com.toanyone.delivery.infrastructure.client.dto.HubFindResponseDto;
import com.toanyone.delivery.infrastructure.client.dto.RouteSegmentDto;
import com.toanyone.delivery.message.DeliveryCompletedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryManagerRepository deliveryManagerRepository;
    private final CustomDeliveryRepository customDeliveryRepository;
    private final CustomDeliveryMangerRepository customDeliveryMangerRepository;
    private final HubClient hubClient;
    private final AiClient aiClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CacheManager cacheManager;

    private static void initialUserContext(Long userId, String userRole, String slackId) {
        UserContext.setCurrentContext(UserContext.builder()
                .role(userRole)
                .userId(userId)
                .slackId(slackId)
                .build());
    }

    public void createDelivery(DeliveryRequestMessage message,
                               Long userId,
                               String userRole,
                               String slackId) throws IOException {
        initialUserContext(userId, userRole, slackId);
        // ** null 검증 코드와 허브로 요청하는 부분을 메서드로 추출 -> rest 통신 발생
        List<RouteSegmentDto> response = getRouteSegmentDtos(message);
        // ** 필요한 담당자를 구하는 경우를 메서드로 추출
        int neededDeliveryManagerCount = getNeededDeliveryManagerCount(response);
        // db 호출 발생
        Delivery lastDeliveryByOrderId = getLastDeliveryByOrderId();

        // 가장 최근의 배송 데이터가 존재하는 경우
        if (validateLastDeliveryExists(lastDeliveryByOrderId)) {
            // ** 조회해온 배송 데이터의 배송 경로 중 가장 마지막 배송 경로를 추출한다. -> 가장 마지막 배송경로를 추출하는 경우를 메서드로 추출한다.
            DeliveryRoad lastDeliveryRoad = getLastDeliveryRoad(lastDeliveryByOrderId);
            //  마지막 배송 경로 데이터 상에 존재하는 허브 배송 담당자의 ID를 추출한다. 이 허브 배송 담당자가 가장 최근에 허브 배송을 담당한 담당자가 된다.
            Long lastOrderedHubDeliveryManagerId = lastDeliveryRoad.getDeliveryManagerId();

            List<DeliveryRoad> deliveryRoads = setUPDeliveryRoadsIfLastDeliveryExists(neededDeliveryManagerCount, response, lastOrderedHubDeliveryManagerId);
            // ** 배송 테이블에서 도착허브ID = 매개변수로 주어진 도착허브 ID의 조건을 만족하면서 가장 마지막에 삽입된 행을 가져온다. -> db 호출
            Delivery lastDeliveryForArrivalHub = getLastDeliveryForArrivalHub(message);
            // 목적지 허브 정보가 최근 배송 정보에 포함되어 있을 경우
            if (validateLastDeliveryForArrivalHubExists(lastDeliveryForArrivalHub)) {
                createDeliveryResponseDtoIfLastDeliveryForArrivalHubExists(slackId, lastDeliveryForArrivalHub, message, deliveryRoads);
            }
            // 목적지 허브가 최근 배송 경로에 포함되어 있지 않을 경우
            createDeliveryResponseDtoIfLastDeliveryForArrivalHubNotExists(slackId, message, deliveryRoads);
        }
        // 최근 배송 정보가 존재하지 않을 경우
        createDeliveryResponseDtoIfLastDeliveryNotExists(slackId, neededDeliveryManagerCount, response, message);
    }

    private void createDeliveryResponseDtoIfLastDeliveryNotExists(String slackId, int neededDeliveryManagerCount, List<RouteSegmentDto> response, DeliveryRequestMessage message) {
        // 가장 최근의 배송 데이터가 존재하지 않는 경우
        List<DeliveryRoad> deliveryRoads = setUpDeliveryRoadsIfLastDeliveryNotExists(neededDeliveryManagerCount, response);
        // ** 배송 테이블에 도착허브 ID = 매개변수로 주어진 도착허브 ID 조건을 만족하는 행이 없는 경우 -> db 호출
        DeliveryManager deliveryManager = getFirstStoreDeliveryManager(message);
        Delivery delivery = createDelivery(slackId, message, deliveryRoads, deliveryManager.getId());
        Delivery savedDelivery = deliveryRepository.save(delivery);

        // ** 슬랙 메시지에 담아줄 배송담당자의 정보를 조회 -> db 호출
        DeliveryManager deliveryPerson = getDeliveryPerson(savedDelivery);

        // ** 경유허브 Id를 허브 클라이언트에 넘기고 경유 허브 정보를 받아온다. -> 메서드 추출
        List<Long> stopOverIds = getStopOverHubIds(delivery, message);

        // 경유지 주소 정보를 담을 목록
        // ** 허브 클라이언트로부터 경유하는 허브에 대한 정보들을 조회해온 후 stopOverAddress에 하나씩 담는다. -> rest 통신 n번 발생
        List<String> stopOverAddress = addStopOverAddress(stopOverIds);

        // 메서드 추출
        RequestCreateMessageDto messageForAiService = createRequestMessageDto(slackId, deliveryPerson, message, stopOverAddress);

        aiClient.sendMessage(messageForAiService);
    }

    private boolean validateLastDeliveryForArrivalHubExists(Delivery lastDeliveryForArrivalHub) {
        return lastDeliveryForArrivalHub != null;
    }

    private boolean validateLastDeliveryExists(Delivery lastDeliveryByOrderId) {
        return lastDeliveryByOrderId != null;
    }

    private void createDeliveryResponseDtoIfLastDeliveryForArrivalHubNotExists(String slackId, DeliveryRequestMessage message, List<DeliveryRoad> deliveryRoads) {
        DeliveryManager deliveryManager = getFirstStoreDeliveryManager(message);
        Delivery delivery = createDelivery(slackId, message, deliveryRoads, deliveryManager.getId());
        Delivery savedDelivery = deliveryRepository.save(delivery);
        // ** 슬랙 메시지에 담아줄 배송담당자의 정보를 조회 -> db 호출
        DeliveryManager deliveryPerson = getDeliveryPerson(savedDelivery);

        // ** 경유허브 Id를 허브 클라이언트에 넘기고 경유 허브 정보를 받아온다. -> 메서드 추출
        List<Long> stopOverIds = getStopOverHubIds(delivery, message);

        // ** 허브 클라이언트로부터 경유하는 허브에 대한 정보들을 조회해온 후 stopOverAddress에 하나씩 담는다. -> rest 통신 n번 발생
        List<String> stopOverAddress = addStopOverAddress(stopOverIds);
        RequestCreateMessageDto messageForAiService = createRequestMessageDto(slackId, deliveryPerson, message, stopOverAddress);
        aiClient.sendMessage(messageForAiService);
        UserContext.clear();
    }

    private void createDeliveryResponseDtoIfLastDeliveryForArrivalHubExists(String slackId, Delivery lastDeliveryForArrivalHub, DeliveryRequestMessage message, List<DeliveryRoad> deliveryRoads) {
        // 해당 행에서 업체 배송 담당자의 아이디를 가져온다.
        Long storeDeliveryManagerId = lastDeliveryForArrivalHub.getStoreDeliveryManagerId();
        long nextStoreDeliveryManagersDeliveryOrder = getNextStoreDeliveryManagersDeliveryOrder(storeDeliveryManagerId);
        Delivery delivery = createDelivery(slackId, message, deliveryRoads, nextStoreDeliveryManagersDeliveryOrder);
        Delivery savedDelivery = deliveryRepository.save(delivery);
        DeliveryManager deliveryPerson = getDeliveryPerson(savedDelivery);
        List<Long> stopOverIds = getStopOverHubIds(delivery, message);
        // 허브 클라이언트로부터 경유하는 허브에 대한 정보들을 조회해온 후 stopOverAddress에 하나씩 담는다.
        List<String> stopOverAddress = addStopOverAddress(stopOverIds);
        // ** 메서드로 추출
        RequestCreateMessageDto messageForAiService = createRequestMessageDto(slackId, deliveryPerson, message, stopOverAddress);
        aiClient.sendMessage(messageForAiService);
        UserContext.clear();
    }

    private Delivery getLastDeliveryForArrivalHub(DeliveryRequestMessage message) {
        return deliveryRepository.findTopByArrivalHubIdOrderByIdDesc(message.getArrivalHubId())
                .orElse(null);
    }

    private List<DeliveryRoad> setUpDeliveryRoadsIfLastDeliveryNotExists(int neededDeliveryManagerCount, List<RouteSegmentDto> response) {
        List<DeliveryRoad> deliveryRoads = new ArrayList<>();
        for (int sequence = 1; sequence < neededDeliveryManagerCount; sequence++) {
//            DeliveryManager hubDeliveryManager = hubDeliveryManagers.get(sequence);
            RouteSegmentDto routeSegmentDto = response.get(sequence - 1);
            deliveryRoads.add(DeliveryRoad.createDeliveryRoad((long) sequence, sequence + 1, routeSegmentDto.getStartHub().getId(),
                    routeSegmentDto.getEndHub().getId(), BigDecimal.valueOf(routeSegmentDto.getDistanceKm()), routeSegmentDto.getEstimatedTime()));
        }
        return deliveryRoads;
    }

    private DeliveryManager getFirstStoreDeliveryManager(DeliveryRequestMessage message) {
        return deliveryManagerRepository.findFirstByHubIdOrderByIdAsc(message.getArrivalHubId())
                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
    }

    private Delivery createDelivery(String slackId, DeliveryRequestMessage message, List<DeliveryRoad> deliveryRoads, long nextStoreDeliveryManagersDeliveryOrder) {
        return Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(), message.getArrivalHubId(), message.getDeliveryAddress(),
                message.getRecipient(), slackId, nextStoreDeliveryManagersDeliveryOrder);
    }

    private String getDepartureHubAddress(DeliveryRequestMessage message) {
        return hubClient.getHubById(message.getDepartureHubId()).getBody().getData().getAddress().getAddress();
    }

    private List<String> addStopOverAddress(List<Long> stopOverIds) {
        List<String> stopOverAddress = new ArrayList<>();
        for (int i = 0; i < stopOverIds.size(); i++) {
            // ** 허브 호출 N번 발생
            ResponseEntity<SingleResponse<HubFindResponseDto>> hubById = hubClient.getHubById(stopOverIds.get(i));
            stopOverAddress.add(hubById.getBody().getData().getAddress().getAddress());
        }
        return stopOverAddress;
    }

    private DeliveryManager getDeliveryPerson(Delivery savedDelivery) {
        return deliveryManagerRepository.findById(savedDelivery.getStoreDeliveryManagerId())
                .get();
    }

    private List<DeliveryRoad> setUPDeliveryRoadsIfLastDeliveryExists(int neededDeliveryManagerCount, List<RouteSegmentDto> response, Long lastOrderedHubDeliveryManagerId) {
        List<DeliveryRoad> deliveryRoads = new ArrayList<>();
        for (int sequence = 1; sequence < neededDeliveryManagerCount; sequence++) {
//                DeliveryManager nextHubDeliveryManager = nextHubDeliveryManagers.get(sequence-1);
            RouteSegmentDto routeSegmentDto = response.get(sequence - 1);
            // 조회해온 허브 배송 담당자들의 ID를 새로 생성하게 될 배송경로에 순차적으로 매핑해준다.
            deliveryRoads.add(DeliveryRoad.createDeliveryRoad((lastOrderedHubDeliveryManagerId + sequence) % 10, sequence, routeSegmentDto.getStartHub().getId(),
                    routeSegmentDto.getEndHub().getId(), BigDecimal.valueOf(routeSegmentDto.getDistanceKm()), routeSegmentDto.getEstimatedTime()));
        }
        return deliveryRoads;
    }

    private DeliveryRoad getLastDeliveryRoad(Delivery lastDeliveryByOrderId) {
        return lastDeliveryByOrderId.getDeliveryRoads().get(lastDeliveryByOrderId.getDeliveryRoads().size() - 1);
    }

    private int getNeededDeliveryManagerCount(List<RouteSegmentDto> response) {
        return response.size();
    }

    private Delivery getLastDeliveryByOrderId() {
        return deliveryRepository.findTopByOrderByIdDesc()
                .orElse(null);
    }

    private List<RouteSegmentDto> getRouteSegmentDtos(DeliveryRequestMessage message) {
        return Objects.requireNonNull(hubClient.findHub(message.getDepartureHubId(), message.getArrivalHubId())
                        .getBody())
                .getData();
    }

    private RequestCreateMessageDto createRequestMessageDto(String slackId, DeliveryManager deliveryPerson, DeliveryRequestMessage message, List<String> stopOverAddress) {
        return RequestCreateMessageDto.builder()
                .deliveryPerson(deliveryPerson.getName())
                .orderId(message.getOrderId())
                .orderNickName(message.getOrdererName())
                .orderSlackId(slackId)
                .itemInfo(getItemInfo(message))
                .request(message.getRequest())
                .destination(message.getDeliveryAddress())
                .deliveryPersonSlackId(slackId)
                .stopOver(stopOverAddress.toString())
                .shippingAddress(getDepartureHubAddress(message))
                .build();
    }

    private String getItemInfo(DeliveryRequestMessage message) {
        return message.getItems().stream()
                .map(item -> String.format("상품 정보 : %s %d박스", item.getItemName(), item.getQuantity()))
                .toList()
                .toString()
                .replaceAll("(^\\[|\\]$)", "");
    }

    private List<Long> getStopOverHubIds(Delivery delivery, DeliveryRequestMessage message) {
        return delivery.getDeliveryRoads()
                .stream()
                .mapToLong(DeliveryRoad::getArrivalHubId)
                .filter(arrivalHubId -> message.getArrivalHubId() != arrivalHubId)
                .boxed()
                .toList();
    }

    private Long getNextStoreDeliveryManagersDeliveryOrder(Long storeDeliveryManagerId) {
        final Long lastStoreDeliveryManagerId = 10L;
        if ((storeDeliveryManagerId + 1) % 10 == 0) {
            return lastStoreDeliveryManagerId;
        }
        return (storeDeliveryManagerId + 1) % 10;
    }

    public Long createDeliveryManager(CreateDeliveryManagerRequestDto request) {
        final Long hubDeliveryManagersHubId = 0L;

        if (deliveryManagerRepository.existsByUserId(request.getUserId())) {
            throw new DeliveryManagerException.AlreadyExistsUserException();
        }
        DeliveryManagerType deliveryManagerType = DeliveryManagerType
                .fromValue(request.getDeliveryManagerType())
                .orElseThrow(DeliveryManagerException.InvalidDeliveryManagerTypeException::new);

        if (deliveryManagerType.equals(DeliveryManagerType.STORE_DELIVERY_MANAGER)) {
            ResponseEntity<SingleResponse<HubFindResponseDto>> response = hubClient.getHubById(request.getHubId());
            return Optional.ofNullable(response.getBody())
                    .map(SingleResponse::getData)
                    .map(hubResponse -> {
                        Long nextDeliveryOrder = customDeliveryMangerRepository.nextDeliveryOrder(hubResponse.getHubId());
                        DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(request.getUserId(), deliveryManagerType,
                                hubResponse.getHubId(), nextDeliveryOrder, request.getName());
                        return deliveryManagerRepository.save(deliveryManager).getId();
                    })
                    .orElseThrow(DeliveryManagerException.InvalidHubException::new);

        }

        Long nextDeliveryOrder = customDeliveryMangerRepository.nextDeliveryOrder(hubDeliveryManagersHubId);
        DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(request.getUserId(), deliveryManagerType,
                hubDeliveryManagersHubId, nextDeliveryOrder, request.getName());

        return deliveryManagerRepository.save(deliveryManager).getId();
    }

    @Transactional(readOnly = true)
    public CursorPage<GetDeliveryResponseDto> getDeliveries(GetDeliverySearchConditionRequestDto request) {
        if (request.getDeliveryStatus() != null) {
            Delivery.DeliveryStatus deliveryStatus = Delivery.DeliveryStatus.fromValue(request.getDeliveryStatus())
                    .orElseThrow(DeliveryException.InvalidDeliveryTypeException::new);
            CursorPage<GetDeliveryResponseDto> responseDtos = customDeliveryRepository.getDeliveries(request.getDeliveryId(), deliveryStatus, request.getDepartureHubId(), request.getArrivalHubId(),
                    request.getRecipient(), request.getStoreDeliveryManagerId(), request.getLimit(), request.getSortBy());
            return responseDtos;
        }
        CursorPage<GetDeliveryResponseDto> deliveries = customDeliveryRepository.getDeliveries(request.getDeliveryId(), null, request.getDepartureHubId(),
                request.getArrivalHubId(), request.getRecipient(), request.getStoreDeliveryManagerId(), request.getLimit(), request.getSortBy());
        return deliveries;
    }

    @Transactional(readOnly = true)
    public GetDeliveryResponseDto getDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(DeliveryException.DeliveryNotFoundException::new);

        GetDeliveryResponseDto response = GetDeliveryResponseDto.from(delivery);
        return response;
    }

    public DeleteDeliveryResponseDto deleteDelivery(Long deliveryId) {
        UserContext userInfo = UserContext.getUserContext();
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(DeliveryException.DeliveryNotFoundException::new);

        if (userInfo.getRole().equals("MASTER")) {
            delivery.deleteDelivery(userInfo.getUserId());
            Delivery deletedDelivery = deliveryRepository.save(delivery);
            return DeleteDeliveryResponseDto.from(deletedDelivery);
        }

        if (userInfo.getRole().equals("HUB")) {
            if (userInfo.getHubId().equals(delivery.getArrivalHubId()) || userInfo.getHubId().equals(delivery.getDepartureHubId())) {
                delivery.deleteDelivery(userInfo.getUserId());
                Delivery deletedDelivery = deliveryRepository.save(delivery);
                return DeleteDeliveryResponseDto.from(deletedDelivery);
            }
        }
        throw new DeliveryException.UnauthorizedDeliveryDeleteException();
    }

    public UpdateDeliveryResponseDto updateDelivery(Long deliveryId, UpdateDeliveryRequestDto request) {

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(DeliveryException.DeliveryNotFoundException::new);

        UserContext userInfo = UserContext.getUserContext();

        Delivery.DeliveryStatus deliveryStatus = Delivery.DeliveryStatus.fromValue(request.getDeliveryStatus())
                .orElseThrow(DeliveryException.InvalidDeliveryTypeException::new);

        if (userInfo.getRole().equals("MASTER")) {
            delivery.updatedDelivery(deliveryStatus, request.getDeliveryAddress(), request.getRecipient(), request.getRecipientSlackId());
            Delivery updatedDelivery = deliveryRepository.save(delivery);
            verifyDeliveryStatus(updatedDelivery);
            return UpdateDeliveryResponseDto.from(delivery);
        }
        if (userInfo.getRole().equals("HUB") && (userInfo.getHubId().equals(delivery.getArrivalHubId()) || userInfo.getHubId().equals(delivery.getDepartureHubId()))) {
            delivery.updatedDelivery(deliveryStatus, request.getDeliveryAddress(), request.getRecipient(), request.getRecipientSlackId());
            Delivery updatedDelivery = deliveryRepository.save(delivery);
            verifyDeliveryStatus(updatedDelivery);
            return UpdateDeliveryResponseDto.from(delivery);
        }
        if (userInfo.getRole().equals("DELIVERY")) {
            DeliveryManager storeDeliveryManager = deliveryManagerRepository
                    .findById(delivery.getStoreDeliveryManagerId())
                    .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
            if (userInfo.getUserId().equals(storeDeliveryManager.getUserId())) {
                delivery.updatedDelivery(deliveryStatus, request.getDeliveryAddress(), request.getRecipient(), request.getRecipientSlackId());
                Delivery updatedDelivery = deliveryRepository.save(delivery);
                verifyDeliveryStatus(updatedDelivery);
                return UpdateDeliveryResponseDto.from(delivery);
            }
        }
        throw new DeliveryException.UnauthorizedDeliveryUpdateException();
    }

    private void verifyDeliveryStatus(Delivery updatedDelivery) {
        if (updatedDelivery.getDeliveryStatus().equals(Delivery.DeliveryStatus.DELIVERY_COMPLETED)) {
            sendDeliveryCompletedMessage(updatedDelivery);
        }
    }

    private void sendDeliveryCompletedMessage(Delivery updatedDelivery) {
        DeliveryCompletedMessage message = DeliveryCompletedMessage.builder()
                .orderId(updatedDelivery.getOrderId())
                .deliveryStatus(Delivery.DeliveryStatus.DELIVERY_COMPLETED.toString())
                .build();
        Message<DeliveryCompletedMessage> kafkaMessage = MessageBuilder.withPayload(message)
                .setHeader("X-User-Role", UserContext.getUserContext().getRole())
                .setHeader("X-User-Id", UserContext.getUserContext().getUserId())
                .setHeader("X-Slack-Id", UserContext.getUserContext().getSlackId())
                .setHeader(KafkaHeaders.TOPIC, "delivery.completed")
                .build();
        kafkaTemplate.send(kafkaMessage);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "deliveryManagerCache", key = "#deliveryManagerId")
    public GetDeliveryManagerResponseDto getDeliveryManager(Long deliveryManagerId) {
        DeliveryManager deliveryManager = deliveryManagerRepository.findById(deliveryManagerId)
                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
        return GetDeliveryManagerResponseDto.from(deliveryManager);
    }

    @Transactional(readOnly = true)
    public CursorPage<GetDeliveryManagerResponseDto> getDeliveryManagers(GetDeliveryManagerSearchConditionRequestDto request) {
        if (request.getDeliveryManagerType() != null) {
            DeliveryManagerType deliveryManagerType = DeliveryManagerType.fromValue(request.getDeliveryManagerType())
                    .orElseThrow(DeliveryManagerException.InvalidDeliveryManagerTypeException::new);
            CursorPage<GetDeliveryManagerResponseDto> responseDtos = customDeliveryMangerRepository.getDeliveryManagers(request.getDeliveryManagerId(), request.getSortBy(),
                    deliveryManagerType, request.getUserId(), request.getName(), request.getLimit());
            return responseDtos;
        }
        CursorPage<GetDeliveryManagerResponseDto> responseDtos = customDeliveryMangerRepository.getDeliveryManagers(request.getDeliveryManagerId(), request.getSortBy(), null, request.getUserId(), request.getName(), request.getLimit());
        return responseDtos;
    }

    @CacheEvict(cacheNames = "deliveryManagerCache", key = "#deliveryManagerId")
    public UpdateDeliveryManagerResponseDto updateDeliveryManager(Long deliveryManagerId, UpdateDeliveryManagerRequestDto request) {
        UserContext userInfo = UserContext.getUserContext();
        DeliveryManager deliveryManager = deliveryManagerRepository.findById(deliveryManagerId)
                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);

        if (userInfo.getRole().equals("MASTER")) {
            deliveryManager.updateName(request.getName());
            DeliveryManager updatedDeliveryManager = deliveryManagerRepository.save(deliveryManager);
            return UpdateDeliveryManagerResponseDto.from(updatedDeliveryManager);
        }

        if (userInfo.getRole().equals("HUB")) {
            if (userInfo.getHubId().equals(deliveryManager.getHubId())) {
                deliveryManager.updateName(request.getName());
                return UpdateDeliveryManagerResponseDto.from(deliveryManagerRepository.save(deliveryManager));
            }
        }
        throw new DeliveryManagerException.UnauthorizedDeliveryManagerEditException();
    }

    @CacheEvict(cacheNames = "deliveryManagerCache", key = "#deliveryManagerId")
    public DeleteDeliveryManagerResponseDto deleteDeliveryManager(Long deliveryManagerId) {
        UserContext userInfo = UserContext.getUserContext();
        DeliveryManager deliveryManager = deliveryManagerRepository.findById(deliveryManagerId)
                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);

        if (userInfo.getRole().equals("MASTER")) {
            deliveryManager.deleteDeliveryManager(userInfo.getUserId());
            DeliveryManager deletedDeliveryManager = deliveryManagerRepository.save(deliveryManager);
            return DeleteDeliveryManagerResponseDto.from(deletedDeliveryManager);
        }

        if (userInfo.getRole().equals("HUB")) {
            if (userInfo.getHubId().equals(deliveryManager.getHubId())) {
                deliveryManager.deleteDeliveryManager(userInfo.getUserId());
                DeliveryManager deletedDeliveryManager = deliveryManagerRepository.save(deliveryManager);
                return DeleteDeliveryManagerResponseDto.from(deletedDeliveryManager);
            }
        }
        throw new DeliveryManagerException.UnauthorizedDeliveryManagerDeleteException();

    }


}
