package com.toanyone.delivery.domain.repository;

import com.toanyone.delivery.application.dtos.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.domain.DeliveryManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomDeliveryMangerRepository {
    Page<GetDeliveryManagerResponseDto> getDeliveryManagers(Pageable pageable, Long DeliveryManagerId, DeliveryManager.DeliveryManagerType deliveryManagerType);
}
