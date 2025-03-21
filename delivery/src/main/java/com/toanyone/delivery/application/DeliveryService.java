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
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import com.toanyone.delivery.domain.repository.CustomDeliveryRepository;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import com.toanyone.delivery.domain.repository.DeliveryRepository;
import com.toanyone.delivery.infrastructure.client.HubClient;
import com.toanyone.delivery.infrastructure.client.dto.GetHubResponseDto;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "delivery.requested", groupId = "delivery")
    public void consumeDeliveryMessage(ConsumerRecord<String, DeliveryRequestMessage> record) throws IOException {

        DeliveryRequestMessage message = record.value();
        System.out.println();
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
            ResponseEntity<SingleResponse<GetHubResponseDto>> response = hubClient.getHubById(request.getHubId());
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
