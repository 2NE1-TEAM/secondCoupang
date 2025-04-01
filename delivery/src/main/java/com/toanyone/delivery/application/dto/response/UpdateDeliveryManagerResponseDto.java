package com.toanyone.delivery.application.dto.response;

import com.toanyone.delivery.domain.DeliveryManager;
import lombok.Getter;

@Getter
public class UpdateDeliveryManagerResponseDto {
    private Long deliveryManagerId;
    private String name;

    public static UpdateDeliveryManagerResponseDto from(final DeliveryManager deliveryManager) {
        final UpdateDeliveryManagerResponseDto responseDto = new UpdateDeliveryManagerResponseDto();
        responseDto.deliveryManagerId = deliveryManager.getId();
        responseDto.name = deliveryManager.getName();
        return responseDto;
    }
}
