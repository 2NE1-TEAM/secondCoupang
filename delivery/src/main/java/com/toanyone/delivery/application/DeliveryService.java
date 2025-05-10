package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dto.request.DeliveryRequestMessage;
import com.toanyone.delivery.application.dto.request.GetDeliverySearchConditionRequestDto;
import com.toanyone.delivery.application.dto.request.RequestCreateMessageDto;
import com.toanyone.delivery.application.dto.request.UpdateDeliveryRequestDto;
import com.toanyone.delivery.application.dto.response.DeleteDeliveryResponseDto;
import com.toanyone.delivery.application.dto.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dto.response.GetDeliveryResponseDto;
import com.toanyone.delivery.application.dto.response.UpdateDeliveryResponseDto;
import com.toanyone.delivery.application.exception.DeliveryException;
import com.toanyone.delivery.application.exception.DeliveryException.InvalidDeliveryTypeException;
import com.toanyone.delivery.application.message.DeliveryCompletedMessage;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.common.utils.SingleResponse;
import com.toanyone.delivery.common.utils.UserContext;
import com.toanyone.delivery.domain.Delivery;
import com.toanyone.delivery.domain.Delivery.DeliveryStatus;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryRoad;
import com.toanyone.delivery.domain.repository.CustomDeliveryRepository;
import com.toanyone.delivery.domain.repository.DeliveryRepository;
import com.toanyone.delivery.infrastructure.client.AiClient;
import com.toanyone.delivery.infrastructure.client.HubClient;
import com.toanyone.delivery.infrastructure.client.dto.HubFindResponseDto;
import com.toanyone.delivery.infrastructure.client.dto.RouteSegmentDto;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

  private final DeliveryManagerService deliveryManagerService;
  private final DeliveryRepository deliveryRepository;
  private final CustomDeliveryRepository customDeliveryRepository;
  private final HubClient hubClient;
  private final AiClient aiClient;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  private static void initialUserContext(Long userId, String userRole, String slackId) {
    UserContext.setCurrentContext(UserContext.builder()
        .role(userRole)
        .userId(userId)
        .slackId(slackId)
        .build());
  }

  @Transactional
  public void createDelivery(DeliveryRequestMessage message, Long userId, String userRole, String slackId) throws IOException {
    initialUserContext(userId, userRole, slackId);

    List<RouteSegmentDto> response = getRouteSegmentDtos(message);
    int neededDeliveryManagerCount = getNeededDeliveryManagerCount(response);
    Delivery lastDeliveryByOrderId = getLastDeliveryByOrderId();

    if (validateLastDeliveryExists(lastDeliveryByOrderId)) {
      DeliveryRoad lastDeliveryRoad = getLastDeliveryRoad(lastDeliveryByOrderId);
      Long lastOrderedHubDeliveryManagerId = lastDeliveryRoad.getDeliveryManagerId();
      List<DeliveryRoad> deliveryRoads = setUPDeliveryRoadsIfLastDeliveryExists(
          neededDeliveryManagerCount, response, lastOrderedHubDeliveryManagerId);
      Delivery lastDeliveryForArrivalHub = getLastDeliveryForArrivalHub(message);

      if (validateLastDeliveryForArrivalHubExists(lastDeliveryForArrivalHub)) {
        createDeliveryResponseDtoIfLastDeliveryForArrivalHubExists(slackId,
            lastDeliveryForArrivalHub, message, deliveryRoads);
        return;
      }
      createDeliveryResponseDtoIfLastDeliveryForArrivalHubNotExists(slackId, message,
          deliveryRoads);
      return;
    }
    createDeliveryResponseDtoIfLastDeliveryNotExists(slackId, neededDeliveryManagerCount, response,
        message);
  }

  @Cacheable(
      cacheNames = "deliveryCursorCache",
      key = "'cursor:' + #request.deliveryId + ':' + #request.sortBy + ':' + #request.deliveryStatus + ':' + #request.departureHubId + ':' + #request.arrivalHubId + ':' + #request.recipient + ':' + #request.storeDeliveryManagerId + ':' + #request.limit"
  )
  @Transactional(readOnly = true)
  public CursorPage<GetDeliveryResponseDto> getDeliveries(
      GetDeliverySearchConditionRequestDto request) {
    if (request.getDeliveryStatus() != null) {
      DeliveryStatus deliveryStatus = DeliveryStatus.fromValue(request.getDeliveryStatus())
          .orElseThrow(InvalidDeliveryTypeException::new);
      CursorPage<Delivery> result = customDeliveryRepository.getDeliveriesWithCursor(
          request.getDeliveryId(), deliveryStatus,
          request.getDepartureHubId(), request.getArrivalHubId(), request.getRecipient(),
          request.getStoreDeliveryManagerId(),
          request.getLimit(), request.getSortBy());

      List<GetDeliveryResponseDto> dtos = result.getContent()
          .stream().map(GetDeliveryResponseDto::from)
          .collect(Collectors.toList());

      return new CursorPage<>(dtos, result.getNextCursor(),
          result.isHasNext());
    }
    CursorPage<Delivery> deliveriesWithCursor = customDeliveryRepository.getDeliveriesWithCursor(
        request.getDeliveryId(), null, request.getDepartureHubId(),
        request.getArrivalHubId(), request.getRecipient(), request.getStoreDeliveryManagerId(),
        request.getLimit(), request.getSortBy());
    List<GetDeliveryResponseDto> getDeliveryResponseDtos = deliveriesWithCursor.getContent()
        .stream().map(GetDeliveryResponseDto::from)
        .collect(Collectors.toList());
    return new CursorPage<>(getDeliveryResponseDtos, deliveriesWithCursor.getNextCursor(),
        deliveriesWithCursor.isHasNext());
  }

  @Transactional(readOnly = true)
  public Page<GetDeliveryResponseDto> getDeliveries(Pageable pageable,
      GetDeliverySearchConditionRequestDto request) {
    DeliveryStatus deliveryStatus = null;

    if (request.getDeliveryStatus() != null) {
      // 문자열 → Enum 변환 시 예외 처리
      deliveryStatus = DeliveryStatus.fromValue(request.getDeliveryStatus())
          .orElseThrow(InvalidDeliveryTypeException::new);
    }

    Page<Delivery> deliveriesWithOffset = customDeliveryRepository.getDeliveriesWithOffset(
        pageable,
        request.getDeliveryId(),
        deliveryStatus,
        request.getDepartureHubId(),
        request.getArrivalHubId(),
        request.getRecipient(),
        request.getStoreDeliveryManagerId(),
        pageable.getPageSize(),
        request.getSortBy()
    );

    List<GetDeliveryResponseDto> deliveryResponseDtos = deliveriesWithOffset.getContent().stream()
        .map(GetDeliveryResponseDto::from)
        .collect(Collectors.toList());

    return new PageImpl<>(deliveryResponseDtos, pageable, deliveriesWithOffset.getTotalElements());
  }


  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "deliveryCache", key = "#deliveryId")
  public GetDeliveryResponseDto getDelivery(Long deliveryId) {
    Delivery delivery = deliveryRepository.findById(deliveryId)
        .orElseThrow(DeliveryException.DeliveryNotFoundException::new);

    GetDeliveryResponseDto response = GetDeliveryResponseDto.from(delivery);
    return response;
  }

  @Transactional
  @CacheEvict(cacheNames = "deliveryCache", key = "#deliveryId")
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
      if (userInfo.getHubId().equals(delivery.getArrivalHubId()) || userInfo.getHubId()
          .equals(delivery.getDepartureHubId())) {
        delivery.deleteDelivery(userInfo.getUserId());
        Delivery deletedDelivery = deliveryRepository.save(delivery);
        return DeleteDeliveryResponseDto.from(deletedDelivery);
      }
    }
    throw new DeliveryException.UnauthorizedDeliveryDeleteException();
  }

  @Transactional
  @Cacheable(cacheNames = "deliveryCache", key = "#deliveryId")
  public UpdateDeliveryResponseDto updateDelivery(Long deliveryId,
      UpdateDeliveryRequestDto request) {

    Delivery delivery = deliveryRepository.findById(deliveryId)
        .orElseThrow(DeliveryException.DeliveryNotFoundException::new);

    UserContext userInfo = UserContext.getUserContext();

    Delivery.DeliveryStatus deliveryStatus = Delivery.DeliveryStatus.fromValue(
            request.getDeliveryStatus())
        .orElseThrow(DeliveryException.InvalidDeliveryTypeException::new);

    if (userInfo.getRole().equals("MASTER")) {
      delivery.updatedDelivery(deliveryStatus, request.getDeliveryAddress(), request.getRecipient(),
          request.getRecipientSlackId());
      Delivery updatedDelivery = deliveryRepository.save(delivery);
      verifyDeliveryStatus(updatedDelivery);
      return UpdateDeliveryResponseDto.from(delivery);
    }
    if (userInfo.getRole().equals("HUB") && (userInfo.getHubId().equals(delivery.getArrivalHubId())
        || userInfo.getHubId().equals(delivery.getDepartureHubId()))) {
      delivery.updatedDelivery(deliveryStatus, request.getDeliveryAddress(), request.getRecipient(),
          request.getRecipientSlackId());
      Delivery updatedDelivery = deliveryRepository.save(delivery);
      verifyDeliveryStatus(updatedDelivery);
      return UpdateDeliveryResponseDto.from(delivery);
    }
    if (userInfo.getRole().equals("DELIVERY")) {
      GetDeliveryManagerResponseDto responseDto = deliveryManagerService.getDeliveryManager(
          delivery.getStoreDeliveryManagerId());
      if (userInfo.getUserId().equals(responseDto.getUserId())) {
        delivery.updatedDelivery(deliveryStatus, request.getDeliveryAddress(),
            request.getRecipient(), request.getRecipientSlackId());
        Delivery updatedDelivery = deliveryRepository.save(delivery);
        verifyDeliveryStatus(updatedDelivery);
        return UpdateDeliveryResponseDto.from(delivery);
      }
    }
    throw new DeliveryException.UnauthorizedDeliveryUpdateException();
  }

  private void createDeliveryResponseDtoIfLastDeliveryNotExists(String slackId,
      int neededDeliveryManagerCount, List<RouteSegmentDto> response,
      DeliveryRequestMessage message) {
    List<DeliveryRoad> deliveryRoads = setUpDeliveryRoadsIfLastDeliveryNotExists(
        neededDeliveryManagerCount, response);
    DeliveryManager deliveryManager = deliveryManagerService.getFirstStoreDeliveryManager(
        message.getArrivalHubId());
    Delivery delivery = createDelivery(slackId, message, deliveryRoads, deliveryManager.getId());
    deliveryRepository.save(delivery);
    List<Long> stopOverIds = getStopOverHubIds(delivery, message);
    List<String> stopOverAddress = addStopOverAddress(stopOverIds);
    RequestCreateMessageDto messageForAiService = createRequestMessageDto(slackId,
        deliveryManager.getName(), message, stopOverAddress);
    aiClient.sendMessage(messageForAiService);
  }

  private boolean validateLastDeliveryForArrivalHubExists(Delivery lastDeliveryForArrivalHub) {
    return lastDeliveryForArrivalHub != null;
  }

  private boolean validateLastDeliveryExists(Delivery lastDeliveryByOrderId) {
    return lastDeliveryByOrderId != null;
  }

  private void createDeliveryResponseDtoIfLastDeliveryForArrivalHubNotExists(String slackId,
      DeliveryRequestMessage message, List<DeliveryRoad> deliveryRoads) {
    DeliveryManager deliveryManager = deliveryManagerService.getFirstStoreDeliveryManager(
        message.getArrivalHubId());
    Delivery delivery = createDelivery(slackId, message, deliveryRoads, deliveryManager.getId());
    deliveryRepository.save(delivery);
    List<Long> stopOverIds = getStopOverHubIds(delivery, message);
    List<String> stopOverAddress = addStopOverAddress(stopOverIds);
    RequestCreateMessageDto messageForAiService = createRequestMessageDto(slackId,
        deliveryManager.getName(), message, stopOverAddress);
    aiClient.sendMessage(messageForAiService);
    UserContext.clear();
  }

  private void createDeliveryResponseDtoIfLastDeliveryForArrivalHubExists(String slackId,
      Delivery lastDeliveryForArrivalHub, DeliveryRequestMessage message,
      List<DeliveryRoad> deliveryRoads) {
    Long storeDeliveryManagerId = lastDeliveryForArrivalHub.getStoreDeliveryManagerId();
    long nextStoreDeliveryManagersDeliveryOrder = getNextStoreDeliveryManagersDeliveryOrder(
        storeDeliveryManagerId);
    Delivery delivery = createDelivery(slackId, message, deliveryRoads,
        nextStoreDeliveryManagersDeliveryOrder);
    deliveryRepository.save(delivery);
    GetDeliveryManagerResponseDto storeDeliveryManager = deliveryManagerService.getDeliveryManager(
        delivery.getStoreDeliveryManagerId());
    List<Long> stopOverIds = getStopOverHubIds(delivery, message);
    List<String> stopOverAddress = addStopOverAddress(stopOverIds);
    RequestCreateMessageDto messageForAiService = createRequestMessageDto(slackId,
        storeDeliveryManager.getName(), message, stopOverAddress);
    aiClient.sendMessage(messageForAiService);
    UserContext.clear();
  }

  private Delivery getLastDeliveryForArrivalHub(DeliveryRequestMessage message) {
    return deliveryRepository.findTopByArrivalHubIdOrderByIdDesc(message.getArrivalHubId())
        .orElse(null);
  }

  private List<DeliveryRoad> setUpDeliveryRoadsIfLastDeliveryNotExists(
      int neededDeliveryManagerCount, List<RouteSegmentDto> response) {
    List<DeliveryRoad> deliveryRoads = new ArrayList<>();
    for (int sequence = 1; sequence <= neededDeliveryManagerCount; sequence++) {
      RouteSegmentDto routeSegmentDto = response.get(sequence - 1);
      deliveryRoads.add(DeliveryRoad.createDeliveryRoad((long) sequence, sequence + 1,
          routeSegmentDto.getStartHub().getId(),
          routeSegmentDto.getEndHub().getId(), BigDecimal.valueOf(routeSegmentDto.getDistanceKm()),
          routeSegmentDto.getEstimatedTime()));
    }
    return deliveryRoads;
  }


  private Delivery createDelivery(String slackId, DeliveryRequestMessage message,
      List<DeliveryRoad> deliveryRoads, long nextStoreDeliveryManagersDeliveryOrder) {
    return Delivery.createDelivery(message.getOrderId(), deliveryRoads, message.getDepartureHubId(),
        message.getArrivalHubId(), message.getDeliveryAddress(),
        message.getRecipient(), slackId, nextStoreDeliveryManagersDeliveryOrder);
  }

  private String getDepartureHubAddress(DeliveryRequestMessage message) {
    return hubClient.getHubById(message.getDepartureHubId()).getBody().getData().getAddress()
        .getAddress();
  }

  private List<String> addStopOverAddress(List<Long> stopOverIds) {
    List<String> stopOverAddress = new ArrayList<>();
    for (int i = 0; i < stopOverIds.size(); i++) {
      ResponseEntity<SingleResponse<HubFindResponseDto>> hubById = hubClient.getHubById(
          stopOverIds.get(i));
      stopOverAddress.add(hubById.getBody().getData().getAddress().getAddress());
    }
    return stopOverAddress;
  }


  private List<DeliveryRoad> setUPDeliveryRoadsIfLastDeliveryExists(int neededDeliveryManagerCount,
      List<RouteSegmentDto> response, Long lastOrderedHubDeliveryManagerId) {
    List<DeliveryRoad> deliveryRoads = new ArrayList<>();
    for (int sequence = 1; sequence <= neededDeliveryManagerCount; sequence++) {
      RouteSegmentDto routeSegmentDto = response.get(sequence - 1);
      deliveryRoads.add(
          DeliveryRoad.createDeliveryRoad((lastOrderedHubDeliveryManagerId + sequence) % 10,
              sequence, routeSegmentDto.getStartHub().getId(),
              routeSegmentDto.getEndHub().getId(),
              BigDecimal.valueOf(routeSegmentDto.getDistanceKm()),
              routeSegmentDto.getEstimatedTime()));
    }
    return deliveryRoads;
  }

  private DeliveryRoad getLastDeliveryRoad(Delivery lastDeliveryByOrderId) {
    return lastDeliveryByOrderId.getDeliveryRoads()
        .get(lastDeliveryByOrderId.getDeliveryRoads().size() - 1);
  }

  private int getNeededDeliveryManagerCount(List<RouteSegmentDto> response) {
    return response.size();
  }

  private Delivery getLastDeliveryByOrderId() {
    return deliveryRepository.findTopByOrderByIdDesc()
        .orElse(null);
  }

  private List<RouteSegmentDto> getRouteSegmentDtos(DeliveryRequestMessage message) {
    return Objects.requireNonNull(
            hubClient.findHub(message.getDepartureHubId(), message.getArrivalHubId())
                .getBody())
        .getData();
  }

  private RequestCreateMessageDto createRequestMessageDto(String slackId, String deliveryPerson,
      DeliveryRequestMessage message, List<String> stopOverAddress) {
    return RequestCreateMessageDto.builder()
        .deliveryPerson(deliveryPerson)
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


}
