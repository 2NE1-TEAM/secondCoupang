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
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
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

    @KafkaListener(topics = "delivery.requested", groupId = "delivery")
    public CreateDeliveryResponseDto consumeDeliveryMessage(ConsumerRecord<String, DeliveryRequestMessage> record) throws IOException {
        DeliveryRequestMessage message = record.value();
        List<RouteSegmentDto> response = Objects.requireNonNull(hubClient.findHub(message.getDepartureHubId(), message.getArrivalHubId())
                        .getBody())
                .getData();
        int neededDeliveryManagerCount = response.size();
        Delivery lastDeliveryByOrderId = deliveryRepository.findTopByOrderByIdDesc()
                .orElse(null);
        List<DeliveryRoad> deliveryRoads = new ArrayList<>();

        // 가장 최근의 배송 데이터가 존재하는 경우
        if (lastDeliveryByOrderId != null) {
            // 조회해온 배송 데이터의 배송 경로 중 가장 마지막 배송 경로를 추출한다.
            DeliveryRoad lastDeliveryRoad = lastDeliveryByOrderId.getDeliveryRoads().get(lastDeliveryByOrderId.getDeliveryRoads().size() - 1);
            //  마지막 배송 경로 데이터 상에 존재하는 허브 배송 담당자의 ID를 추출한다. 이 허브 배송 담당자가 가장 최근에 허브 배송을 담당한 담당자가 된다.
            Long lastOrderedHubDeliveryManagerId = lastDeliveryRoad.getDeliveryManagerId();
            // 최근 허브 배송 담당자의 ID를 배송담당자 테이블에 던져 해당 허브 배송 담당자의 정보를 가져온다.
            DeliveryManager lastOrderedHubDeliveryManager = deliveryManagerRepository.findByDeliveryManagerTypeAndId(DeliveryManagerType.HUB_DELIVERY_MANAGER, lastOrderedHubDeliveryManagerId)
                    .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
            // 최근 허브 배송 담당자의 (배송순번 + 1, 2 ... N) % 10을 통해 다음 배송순번 담당자를 구한다.
            List<Long> nextHubDeliveryManagersDeliveryOrders = new ArrayList<>();
            for (int i = 1; i <= neededDeliveryManagerCount; i++) {
                long nextHubDeliveryManagersDeliveryOrder = (lastOrderedHubDeliveryManager.getDeliveryOrder() + i) % 10;
                // (lastOrderedHubDeliveryManager.getDeliveryOrder() + i) % 10의 결과가 0일 경우 다음 배송순번이 10번인 경우다.
                // 이 경우는 배송순번 10번을 직접 할당해준다.
                if (nextHubDeliveryManagersDeliveryOrder == 0) {
                    final long lastHubDeliveryManagerOrder = 10L;
                    nextHubDeliveryManagersDeliveryOrders.add(lastHubDeliveryManagerOrder);
                } else {
                    nextHubDeliveryManagersDeliveryOrders.add(nextHubDeliveryManagersDeliveryOrder);
                }
            }
            // 필요한 허브 배송 담당자가 2명이고, 최근 허브 배송 담당자의 배송순번이 6번이었을 경우 다음 배송 담당자는 (6+1) % 10, (6+2) % 10인 7, 8 번이 된다.
            // 배송 담당자 테이블에서 배송 담당자 타입이 허브 배송 담당자이면서 배송 순번이 7, 8번인 허브 배송담당자의 정보를 조회해온다.
            List<DeliveryManager> nextHubDeliveryManagers = deliveryManagerRepository.findByDeliveryManagerTypeAndDeliveryOrderIn(DeliveryManagerType.HUB_DELIVERY_MANAGER, nextHubDeliveryManagersDeliveryOrders);
            for (int sequence = 0; sequence < neededDeliveryManagerCount; sequence++) {
                DeliveryManager nextHubDeliveryManager = nextHubDeliveryManagers.get(sequence);
                RouteSegmentDto routeSegmentDto = response.get(sequence);
                // 조회해온 허브 배송 담당자들의 ID를 새로 생성하게 될 배송경로에 순차적으로 매핑해준다.
                deliveryRoads.add(DeliveryRoad.createDeliveryRoad(nextHubDeliveryManager.getId(), sequence + 1, routeSegmentDto.getStartHub().getId(),
                        routeSegmentDto.getEndHub().getId(), BigDecimal.valueOf(routeSegmentDto.getDistanceKm()), routeSegmentDto.getEstimatedTime()));
            }
            // 배송 테이블에서 도착허브ID = 매개변수로 주어진 도착허브 ID의 조건을 만족하면서 가장 마지막에 삽입된 행을 가져온다.
            Delivery lastDeliveryForArrivalHub = deliveryRepository.findTopByArrivalHubIdOrderByIdDesc(message.getArrivalHubId())
                    .orElse(null);
            if (lastDeliveryForArrivalHub != null) {
                // 해당 행에서 업체 배송 담당자의 아이디를 가져온다.
                Long storeDeliveryManagerId = lastDeliveryForArrivalHub.getStoreDeliveryManagerId();
                // 업체 배송 담당자 ID를 통해 해당 업체 배송 담당자 정보를 조회해온다.
                DeliveryManager lastStoreDeliveryManagerFroArrivalHub = deliveryManagerRepository.findById(storeDeliveryManagerId)
                        .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
                long nextStoreDeliveryManagersDeliveryOrder = (lastStoreDeliveryManagerFroArrivalHub.getDeliveryOrder() + 1) % 10;
                // 조회해온 업체 배송 담당자의 (배송 순번 + 1) % 10에 해당하는 배송순번을 구한 후 배송담당자 테이블에서 도착허브 ID와 배송순번을 조건으로 하여 다음 업체 배송 담당자 정보를 불러온다.
                DeliveryManager nextStoreDeliveryManager = deliveryManagerRepository.findByHubIdAndDeliveryOrder(lastStoreDeliveryManagerFroArrivalHub.getHubId(), nextStoreDeliveryManagersDeliveryOrder)
                        .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
                // 배송 데이터를 생성한다.
                Delivery delivery = Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(), message.getArrivalHubId(), message.getDeliveryAddress(),
                        message.getRecipient(), "exampleValue", nextStoreDeliveryManager.getId());
                Delivery savedDelivery = deliveryRepository.save(delivery);

                DeliveryRoad deliveryRoad = delivery.getDeliveryRoads()
                        .stream().findFirst()
                        .get();
                // 배송담당자 정보
                DeliveryManager deliveryPerson = deliveryManagerRepository.findById(deliveryRoad.getDeliveryManagerId())
                        .get();

                // 경유허브 Id를 허브 클라이언트에 넘기고 경유 허브 정보를 받아온다.
                List<Long> stopOverIds = delivery.getDeliveryRoads()
                        .stream()
                        .mapToLong(DeliveryRoad::getDepartureHubId)
                        .filter(departureHubId -> departureHubId != message.getArrivalHubId())
                        .boxed()
                        .toList();
                // 경유지 주소 정보를 담는다.
                List<String> stopOverAddress = new ArrayList<>();

                for (int i = 0; i < stopOverIds.size(); i++) {
                    ResponseEntity<SingleResponse<HubFindResponseDto>> hubById = hubClient.getHubById(stopOverIds.get(i));
                    stopOverAddress.add(hubById.getBody().getData().getAddress().getAddress());
                }

                // 발송 허브 정보를 받아온다.
                String departureHubAddress = hubClient.getHubById(message.getDepartureHubId()).getBody().getData().getAddress().getAddress();


                String itemInfo = message.getItems().stream()
                        .map(item -> String.format("상품 정보 : %s %d박스", item.getItemName(), item.getQuantity()))
                        .toList()
                        .toString()
                        .replaceAll("(^\\[|\\]$)", "");

                RequestCreateMessageDto messageForAiService = RequestCreateMessageDto.builder()
                        .deliveryPerson(deliveryPerson.getName())
                        .orderId(message.getOrderId())
                        .orderNickName(message.getOrdererName())
                        .orderSlackId(UserContext.getUserContext().getSlackId())
                        .itemInfo(itemInfo)
                        .request(message.getRequest())
                        .destination(message.getDeliveryAddress())
                        .deliveryPersonSlackId(UserContext.getUserContext().getSlackId())
                        .stopOver(stopOverAddress.toString())
                        .shippingAddress(departureHubAddress)
                        .build();

                aiClient.sendMessage(messageForAiService);

                return CreateDeliveryResponseDto.from(savedDelivery.getId());
            }
            // 배송 테이블에 도착허브 ID = 매개변수로 주어진 도착허브 ID 조건을 만족하는 행이 없는 경우
            DeliveryManager deliveryManager = deliveryManagerRepository.findFirstByHubIdOrderByIdAsc(message.getArrivalHubId())
                    .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
            Delivery delivery = Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(), message.getArrivalHubId(),
                    message.getDeliveryAddress(), message.getRecipient(), "exampleValue", deliveryManager.getId());
            Delivery savedDelivery = deliveryRepository.save(delivery);
            return CreateDeliveryResponseDto.from(savedDelivery.getId());
        }
        // 가장 최근의 배송 데이터가 존재하지 않는 경우
        // 배송 담당자 테이블에서 허브 배송담당자 타입이면서, 배송순번 <= 필요한 허브 배송 담당자의 수보다 작은 조건을 만족하는 허브 배송 담당자들을 조회해온다.
        List<DeliveryManager> hubDeliveryManagers = deliveryManagerRepository.findByDeliveryManagerTypeAndDeliveryOrderLessThanEqual(DeliveryManagerType.HUB_DELIVERY_MANAGER, neededDeliveryManagerCount);
        // 배송 경로에 조회해온 허브 배송 담당자의 정보를 매핑한다.
        for (int sequence = 0; sequence < neededDeliveryManagerCount; sequence++) {
            DeliveryManager hubDeliveryManager = hubDeliveryManagers.get(sequence);
            RouteSegmentDto routeSegmentDto = response.get(sequence);
            deliveryRoads.add(DeliveryRoad.createDeliveryRoad(hubDeliveryManager.getId(), sequence + 1, routeSegmentDto.getStartHub().getId(),
                    routeSegmentDto.getEndHub().getId(), BigDecimal.valueOf(routeSegmentDto.getDistanceKm()), routeSegmentDto.getEstimatedTime()));
        }
        // 배송 테이블에서 도착허브ID = 매개변수로 주어진 도착허브 ID의 조건을 만족하면서 가장 마지막에 삽입된 행을 가져온다.
        Delivery lastDeliveryForArrivalHub = deliveryRepository.findTopByArrivalHubIdOrderByIdDesc(message.getArrivalHubId())
                .orElse(null);
        if (lastDeliveryForArrivalHub != null) {
            // 해당 행에서 업체 배송 담당자의 아이디를 가져온다.
            Long storeDeliveryManagerId = lastDeliveryForArrivalHub.getStoreDeliveryManagerId();
            // 업체 배송 담당자 ID를 통해 해당 업체 배송 담당자 정보를 조회해온다.
            DeliveryManager lastStoreDeliveryManagerFroArrivalHub = deliveryManagerRepository.findById(storeDeliveryManagerId)
                    .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
            long nextStoreDeliveryManagersDeliveryOrder = lastStoreDeliveryManagerFroArrivalHub.getDeliveryOrder() + 1 % 10;
            // 조회해온 업체 배송 담당자의 (배송 순번 + 1) % 10에 해당하는 배송순번을 구한 후 배송담당자 테이블에서 도착허브 ID와 배송순번을 조건으로 하여 다음 업체 배송 담당자 정보를 불러온다.
            DeliveryManager nextStoreDeliveryManager = deliveryManagerRepository.findByHubIdAndDeliveryOrder(lastDeliveryForArrivalHub.getId(), nextStoreDeliveryManagersDeliveryOrder)
                    .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
            // 배송 데이터를 생성한다.
            Delivery delivery = Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(), message.getArrivalHubId(), message.getDeliveryAddress(),
                    message.getRecipient(), "exampleValue", nextStoreDeliveryManager.getId());
            Delivery savedDelivery = deliveryRepository.save(delivery);
            return CreateDeliveryResponseDto.from(savedDelivery.getId());
        }
        // 배송 테이블에 도착허브 ID = 매개변수로 주어진 도착허브 ID 조건을 만족하는 행이 없는 경우
        DeliveryManager deliveryManager = deliveryManagerRepository.findFirstByHubIdOrderByIdAsc(message.getArrivalHubId())
                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
        Delivery delivery = Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(), message.getArrivalHubId(),
                message.getDeliveryAddress(), message.getRecipient(), "exampleValue", deliveryManager.getId());
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return CreateDeliveryResponseDto.from(savedDelivery.getId());
    }

//    public CreateDeliveryResponseDto temp(DeliveryRequestMessage message) throws IOException {
//        List<RouteSegmentDto> response = Objects.requireNonNull(hubClient.findHub(message.getDepartureHubId(), message.getArrivalHubId())
//                        .getBody())
//                .getData();
//        int neededDeliveryManagerCount = response.size();
//        Delivery lastDeliveryByOrderId = deliveryRepository.findTopByOrderByIdDesc()
//                .orElse(null);
//        List<DeliveryRoad> deliveryRoads = new ArrayList<>();
//
//        // 가장 최근의 배송 데이터가 존재하는 경우
//        if (lastDeliveryByOrderId != null) {
//            // 조회해온 배송 데이터의 배송 경로 중 가장 마지막 배송 경로를 추출한다.
//            DeliveryRoad lastDeliveryRoad = lastDeliveryByOrderId.getDeliveryRoads().get(lastDeliveryByOrderId.getDeliveryRoads().size() - 1);
//            //  마지막 배송 경로 데이터 상에 존재하는 허브 배송 담당자의 ID를 추출한다. 이 허브 배송 담당자가 가장 최근에 허브 배송을 담당한 담당자가 된다.
//            Long lastOrderedHubDeliveryManagerId = lastDeliveryRoad.getDeliveryManagerId();
//            // 최근 허브 배송 담당자의 ID를 배송담당자 테이블에 던져 해당 허브 배송 담당자의 정보를 가져온다.
//            DeliveryManager lastOrderedHubDeliveryManager = deliveryManagerRepository.findByDeliveryManagerTypeAndId(DeliveryManagerType.HUB_DELIVERY_MANAGER, lastOrderedHubDeliveryManagerId)
//                    .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
//            // 최근 허브 배송 담당자의 (배송순번 + 1, 2 ... N) % 10을 통해 다음 배송순번 담당자를 구한다.
//            List<Long> nextHubDeliveryManagersDeliveryOrders = new ArrayList<>();
//            for (int i = 1; i <= neededDeliveryManagerCount; i++) {
//                long nextHubDeliveryManagersDeliveryOrder = (lastOrderedHubDeliveryManager.getDeliveryOrder() + i) % 10;
//                // (lastOrderedHubDeliveryManager.getDeliveryOrder() + i) % 10의 결과가 0일 경우 다음 배송순번이 10번인 경우다.
//                // 이 경우는 배송순번 10번을 직접 할당해준다.
//                if (nextHubDeliveryManagersDeliveryOrder == 0) {
//                    final long lastHubDeliveryManagerOrder = 10L;
//                    nextHubDeliveryManagersDeliveryOrders.add(lastHubDeliveryManagerOrder);
//                } else {
//                    nextHubDeliveryManagersDeliveryOrders.add(nextHubDeliveryManagersDeliveryOrder);
//                }
//            }
//            // 필요한 허브 배송 담당자가 2명이고, 최근 허브 배송 담당자의 배송순번이 6번이었을 경우 다음 배송 담당자는 (6+1) % 10, (6+2) % 10인 7, 8 번이 된다.
//            // 배송 담당자 테이블에서 배송 담당자 타입이 허브 배송 담당자이면서 배송 순번이 7, 8번인 허브 배송담당자의 정보를 조회해온다.
//            List<DeliveryManager> nextHubDeliveryManagers = deliveryManagerRepository.findByDeliveryManagerTypeAndDeliveryOrderIn(DeliveryManagerType.HUB_DELIVERY_MANAGER, nextHubDeliveryManagersDeliveryOrders);
//            for (int sequence = 0; sequence < neededDeliveryManagerCount; sequence++) {
//                DeliveryManager nextHubDeliveryManager = nextHubDeliveryManagers.get(sequence);
//                RouteSegmentDto routeSegmentDto = response.get(sequence);
//                // 조회해온 허브 배송 담당자들의 ID를 새로 생성하게 될 배송경로에 순차적으로 매핑해준다.
//                deliveryRoads.add(DeliveryRoad.createDeliveryRoad(nextHubDeliveryManager.getId(), sequence + 1, routeSegmentDto.getStartHub().getId(),
//                        routeSegmentDto.getEndHub().getId(), BigDecimal.valueOf(routeSegmentDto.getDistanceKm()), routeSegmentDto.getEstimatedTime()));
//            }
//            // 배송 테이블에서 도착허브ID = 매개변수로 주어진 도착허브 ID의 조건을 만족하면서 가장 마지막에 삽입된 행을 가져온다.
//            Delivery lastDeliveryForArrivalHub = deliveryRepository.findTopByArrivalHubIdOrderByIdDesc(message.getArrivalHubId())
//                    .orElse(null);
//            if (lastDeliveryForArrivalHub != null) {
//                // 해당 행에서 업체 배송 담당자의 아이디를 가져온다.
//                Long storeDeliveryManagerId = lastDeliveryForArrivalHub.getStoreDeliveryManagerId();
//                // 업체 배송 담당자 ID를 통해 해당 업체 배송 담당자 정보를 조회해온다.
//                DeliveryManager lastStoreDeliveryManagerFroArrivalHub = deliveryManagerRepository.findById(storeDeliveryManagerId)
//                        .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
//                long nextStoreDeliveryManagersDeliveryOrder = (lastStoreDeliveryManagerFroArrivalHub.getDeliveryOrder() + 1) % 10;
//                // 조회해온 업체 배송 담당자의 (배송 순번 + 1) % 10에 해당하는 배송순번을 구한 후 배송담당자 테이블에서 도착허브 ID와 배송순번을 조건으로 하여 다음 업체 배송 담당자 정보를 불러온다.
//                DeliveryManager nextStoreDeliveryManager = deliveryManagerRepository.findByHubIdAndDeliveryOrder(lastStoreDeliveryManagerFroArrivalHub.getHubId(), nextStoreDeliveryManagersDeliveryOrder)
//                        .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
//                // 배송 데이터를 생성한다.
//                Delivery delivery = Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(), message.getArrivalHubId(), message.getDeliveryAddress(),
//                        message.getRecipient(), "exampleValue", nextStoreDeliveryManager.getId());
//                Delivery savedDelivery = deliveryRepository.save(delivery);
//
//                DeliveryRoad deliveryRoad = delivery.getDeliveryRoads()
//                        .stream().findFirst()
//                        .get();
//                // 배송담당자 정보
//                DeliveryManager deliveryPerson = deliveryManagerRepository.findById(deliveryRoad.getDeliveryManagerId())
//                        .get();
//
//                // 경유허브 Id를 허브 클라이언트에 넘기고 경유 허브 정보를 받아온다.
//                List<Long> stopOverIds = delivery.getDeliveryRoads()
//                        .stream()
//                        .mapToLong(DeliveryRoad::getDepartureHubId)
//                        .filter(departureHubId -> departureHubId != message.getArrivalHubId())
//                        .boxed()
//                        .toList();
//                // 경유지 주소 정보를 담는다.
//                List<String> stopOverAddress = new ArrayList<>();
//
//                for (int i = 0; i < stopOverIds.size(); i++) {
//                    ResponseEntity<SingleResponse<HubFindResponseDto>> hubById = hubClient.getHubById(stopOverIds.get(i));
//                    stopOverAddress.add(hubById.getBody().getData().getAddress().getAddress());
//                }
//
//                // 발송 허브 정보를 받아온다.
//                String departureHubAddress = hubClient.getHubById(message.getDepartureHubId()).getBody().getData().getAddress().getAddress();
//
//
//                String itemInfo = message.getItems().stream()
//                        .map(item -> String.format("상품 정보 : %s %d박스", item.getItemName(), item.getQuantity()))
//                        .toList()
//                        .toString()
//                        .replaceAll("(^\\[|\\]$)", "");
//
//                RequestCreateMessageDto messageForAiService = RequestCreateMessageDto.builder()
//                        .deliveryPerson(deliveryPerson.getName())
//                        .orderId(message.getOrderId())
//                        .orderNickName(message.getOrdererName())
//                        .orderSlackId(UserContext.getUserContext().getSlackId())
//                        .itemInfo(itemInfo)
//                        .request(message.getRequest())
//                        .destination(message.getDeliveryAddress())
//                        .deliveryPersonSlackId(UserContext.getUserContext().getSlackId())
//                        .stopOver(stopOverAddress.toString())
//                        .shippingAddress(departureHubAddress)
//                        .build();
//
//                aiClient.sendMessage(messageForAiService);
//
//                return CreateDeliveryResponseDto.from(savedDelivery.getId());
//            }
//            // 배송 테이블에 도착허브 ID = 매개변수로 주어진 도착허브 ID 조건을 만족하는 행이 없는 경우
//            DeliveryManager deliveryManager = deliveryManagerRepository.findFirstByHubIdOrderByIdAsc(message.getArrivalHubId())
//                    .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
//            Delivery delivery = Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(), message.getArrivalHubId(),
//                    message.getDeliveryAddress(), message.getRecipient(), "exampleValue", deliveryManager.getId());
//            Delivery savedDelivery = deliveryRepository.save(delivery);
//            return CreateDeliveryResponseDto.from(savedDelivery.getId());
//        }
//        // 가장 최근의 배송 데이터가 존재하지 않는 경우
//        // 배송 담당자 테이블에서 허브 배송담당자 타입이면서, 배송순번 <= 필요한 허브 배송 담당자의 수보다 작은 조건을 만족하는 허브 배송 담당자들을 조회해온다.
//        List<DeliveryManager> hubDeliveryManagers = deliveryManagerRepository.findByDeliveryManagerTypeAndDeliveryOrderLessThanEqual(DeliveryManagerType.HUB_DELIVERY_MANAGER, neededDeliveryManagerCount);
//        // 배송 경로에 조회해온 허브 배송 담당자의 정보를 매핑한다.
//        for (int sequence = 0; sequence < neededDeliveryManagerCount; sequence++) {
//            DeliveryManager hubDeliveryManager = hubDeliveryManagers.get(sequence);
//            RouteSegmentDto routeSegmentDto = response.get(sequence);
//            deliveryRoads.add(DeliveryRoad.createDeliveryRoad(hubDeliveryManager.getId(), sequence + 1, routeSegmentDto.getStartHub().getId(),
//                    routeSegmentDto.getEndHub().getId(), BigDecimal.valueOf(routeSegmentDto.getDistanceKm()), routeSegmentDto.getEstimatedTime()));
//        }
//        // 배송 테이블에서 도착허브ID = 매개변수로 주어진 도착허브 ID의 조건을 만족하면서 가장 마지막에 삽입된 행을 가져온다.
//        Delivery lastDeliveryForArrivalHub = deliveryRepository.findTopByArrivalHubIdOrderByIdDesc(message.getArrivalHubId())
//                .orElse(null);
//        if (lastDeliveryForArrivalHub != null) {
//            // 해당 행에서 업체 배송 담당자의 아이디를 가져온다.
//            Long storeDeliveryManagerId = lastDeliveryForArrivalHub.getStoreDeliveryManagerId();
//            // 업체 배송 담당자 ID를 통해 해당 업체 배송 담당자 정보를 조회해온다.
//            DeliveryManager lastStoreDeliveryManagerFroArrivalHub = deliveryManagerRepository.findById(storeDeliveryManagerId)
//                    .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
//            long nextStoreDeliveryManagersDeliveryOrder = lastStoreDeliveryManagerFroArrivalHub.getDeliveryOrder() + 1 % 10;
//            // 조회해온 업체 배송 담당자의 (배송 순번 + 1) % 10에 해당하는 배송순번을 구한 후 배송담당자 테이블에서 도착허브 ID와 배송순번을 조건으로 하여 다음 업체 배송 담당자 정보를 불러온다.
//            DeliveryManager nextStoreDeliveryManager = deliveryManagerRepository.findByHubIdAndDeliveryOrder(lastDeliveryForArrivalHub.getId(), nextStoreDeliveryManagersDeliveryOrder)
//                    .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
//            // 배송 데이터를 생성한다.
//            Delivery delivery = Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(), message.getArrivalHubId(), message.getDeliveryAddress(),
//                    message.getRecipient(), "exampleValue", nextStoreDeliveryManager.getId());
//            Delivery savedDelivery = deliveryRepository.save(delivery);
//            return CreateDeliveryResponseDto.from(savedDelivery.getId());
//        }
//        // 배송 테이블에 도착허브 ID = 매개변수로 주어진 도착허브 ID 조건을 만족하는 행이 없는 경우
//        DeliveryManager deliveryManager = deliveryManagerRepository.findFirstByHubIdOrderByIdAsc(message.getArrivalHubId())
//                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
//        Delivery delivery = Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(), message.getArrivalHubId(),
//                message.getDeliveryAddress(), message.getRecipient(), "exampleValue", deliveryManager.getId());
//        Delivery savedDelivery = deliveryRepository.save(delivery);
//        return CreateDeliveryResponseDto.from(savedDelivery.getId());
//    }


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
                .completedDeliveryId(updatedDelivery.getId())
                .message("배송이 완료되었습니다.")
                .build();
        Message<DeliveryCompletedMessage> kafkaMessage = MessageBuilder.withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "delivery.completed")
                .build();
        kafkaTemplate.send(kafkaMessage);
    }

    @Transactional(readOnly = true)
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
