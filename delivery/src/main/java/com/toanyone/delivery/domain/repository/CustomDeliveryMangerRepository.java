package com.toanyone.delivery.domain.repository;

import com.toanyone.delivery.application.dtos.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.domain.DeliveryManager;

public interface CustomDeliveryMangerRepository {
    CursorPage<GetDeliveryManagerResponseDto> getDeliveryManagers(Long deliveryManagerId, String sortBy, DeliveryManager.DeliveryManagerType deliveryManagerType, int limit);
}
