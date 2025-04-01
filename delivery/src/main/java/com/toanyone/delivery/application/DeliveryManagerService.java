package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dto.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dto.request.GetDeliveryManagerSearchConditionRequestDto;
import com.toanyone.delivery.application.dto.request.UpdateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dto.response.DeleteDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dto.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dto.response.UpdateDeliveryManagerResponseDto;
import com.toanyone.delivery.application.exception.DeliveryManagerException;
import com.toanyone.delivery.common.utils.MultiResponse;
import com.toanyone.delivery.common.utils.SingleResponse;
import com.toanyone.delivery.common.utils.UserContext;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import com.toanyone.delivery.infrastructure.client.HubClient;
import com.toanyone.delivery.infrastructure.client.dto.HubFindResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DeliveryManagerService {
    private final DeliveryManagerRepository deliveryManagerRepository;
    private final CustomDeliveryMangerRepository customDeliveryMangerRepository;
    private final HubClient hubClient;

    @Transactional
    public Long createDeliveryManager(CreateDeliveryManagerRequestDto request) {
        final Long hubDeliveryManagersHubId = 0L;

        if (deliveryManagerRepository.existsByUserId(request.getUserId())) {
            throw new DeliveryManagerException.AlreadyExistsUserException();
        }
        DeliveryManager.DeliveryManagerType deliveryManagerType = DeliveryManager.DeliveryManagerType
                .fromValue(request.getDeliveryManagerType())
                .orElseThrow(DeliveryManagerException.InvalidDeliveryManagerTypeException::new);

        if (deliveryManagerType.equals(DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER)) {
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
    @Cacheable(cacheNames = "deliveryManagerCache", key = "#deliveryManagerId")
    public GetDeliveryManagerResponseDto getDeliveryManager(Long deliveryManagerId) {
        DeliveryManager deliveryManager = deliveryManagerRepository.findById(deliveryManagerId)
                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
        return GetDeliveryManagerResponseDto.from(deliveryManager);
    }

    @Transactional(readOnly = true)
    public MultiResponse.CursorPage<GetDeliveryManagerResponseDto> getDeliveryManagers(GetDeliveryManagerSearchConditionRequestDto request) {
        if (request.getDeliveryManagerType() != null) {
            DeliveryManager.DeliveryManagerType deliveryManagerType = DeliveryManager.DeliveryManagerType.fromValue(request.getDeliveryManagerType())
                    .orElseThrow(DeliveryManagerException.InvalidDeliveryManagerTypeException::new);
            MultiResponse.CursorPage<GetDeliveryManagerResponseDto> responseDtos = customDeliveryMangerRepository.getDeliveryManagers(request.getDeliveryManagerId(), request.getSortBy(),
                    deliveryManagerType, request.getUserId(), request.getName(), request.getLimit());
            return responseDtos;
        }
        MultiResponse.CursorPage<GetDeliveryManagerResponseDto> responseDtos = customDeliveryMangerRepository.getDeliveryManagers(request.getDeliveryManagerId(), request.getSortBy(), null, request.getUserId(), request.getName(), request.getLimit());
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

    public DeliveryManager getFirstStoreDeliveryManager(Long arrivalHubId) {
        return deliveryManagerRepository.findFirstByHubIdOrderByIdAsc(arrivalHubId)
                .orElseThrow(DeliveryManagerException.NotFoundManagerException::new);
    }


}
