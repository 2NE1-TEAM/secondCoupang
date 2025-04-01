package com.toanyone.delivery.domain.repository;

import com.toanyone.delivery.application.dto.response.GetDeliveryResponseDto;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.domain.Delivery;

public interface CustomDeliveryRepository {

    CursorPage<GetDeliveryResponseDto> getDeliveries(Long deliveryId, Delivery.DeliveryStatus deliveryStatus, Long departureHubId, Long arrivalHubId,
                                                     String recipient, Long storeDeliveryManagerId, int limit, String sortBy);
}
