package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dtos.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dtos.request.GetDeliveryManagerSearchConditionRequestDto;
import com.toanyone.delivery.application.dtos.request.UpdateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dtos.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dtos.response.UpdateDeliveryManagerResponseDto;
import com.toanyone.delivery.application.exception.DeliveryManagerException;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.common.utils.UserContext;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryManager.DeliveryManagerType;
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import com.toanyone.delivery.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryManagerRepository deliveryManagerRepository;
    private final CustomDeliveryMangerRepository customDeliveryMangerRepository;

    public Long createDeliveryManager(CreateDeliveryManagerRequestDto request) {
        if (deliveryManagerRepository.existsByUserId(request.getUserId())) {
            throw new DeliveryManagerException.AlreadyExistsUserException();
        }
        DeliveryManagerType deliveryManagerType = DeliveryManagerType
                .fromValue(request.getDeliveryManagerType())
                .orElseThrow(DeliveryManagerException.InvalidDeliveryManagerTypeException::new);
        Long nextDeliveryOrder = customDeliveryMangerRepository.nextDeliveryOrder(request.getUserId());
        DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(request.getUserId(), deliveryManagerType,
                request.getHubId(), nextDeliveryOrder, request.getName());
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

//    public Long deleteDeliveryManager(Long deliveryManagerId) {
//
//        DeliveryManager deliveryManager = deliveryManagerRepository.findById(deliveryManagerId)
//                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
//
//        deliveryManager.deleteDeliveryManager(UserContext.getUserContext().getUserId());
//        return deliveryManagerId;
//    }


}
