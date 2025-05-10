package com.toanyone.delivery.domain.repository;

import com.toanyone.delivery.application.dto.response.GetDeliveryResponseDto;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.domain.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomDeliveryRepository {

    CursorPage<Delivery> getDeliveriesWithCursor(Long deliveryId, Delivery.DeliveryStatus deliveryStatus, Long departureHubId, Long arrivalHubId,
                                                     String recipient, Long storeDeliveryManagerId, int limit, String sortBy);
    Page<Delivery> getDeliveriesWithOffset(Pageable pageable, Long deliveryId, Delivery.DeliveryStatus deliveryStatus, Long departureHubId, Long arrivalHubId,
        String recipient, Long storeDeliveryManagerId, int limit, String sortBy);
}
