package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dtos.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dtos.request.GetDeliveryManagerSearchConditionRequestDto;
import com.toanyone.delivery.application.dtos.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.application.exception.DeliveryManagerException;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.common.utils.SingleResponse;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryManager.DeliveryManagerType;
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import com.toanyone.delivery.domain.repository.DeliveryRepository;
import com.toanyone.delivery.infrastructure.client.HubClient;
import com.toanyone.delivery.infrastructure.client.dto.GetHubResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryManagerRepository deliveryManagerRepository;
    private final CustomDeliveryMangerRepository customDeliveryMangerRepository;
    private final HubClient hubClient;

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

//    public Long deleteDeliveryManager(Long deliveryManagerId) {
//
//        DeliveryManager deliveryManager = deliveryManagerRepository.findById(deliveryManagerId)
//                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
//
//        deliveryManager.deleteDeliveryManager(UserContext.getUserContext().getUserId());
//        return deliveryManagerId;
//    }


}
