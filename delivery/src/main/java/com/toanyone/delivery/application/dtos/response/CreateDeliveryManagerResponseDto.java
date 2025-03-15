package com.toanyone.delivery.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CreateDeliveryManagerResponseDto {
    @JsonProperty("delivery_manager_id")
    private Long deliveryManagerId;

    public static CreateDeliveryManagerResponseDto from(Long deliveryManagerId) {
        final CreateDeliveryManagerResponseDto dto = new CreateDeliveryManagerResponseDto();
        dto.deliveryManagerId = deliveryManagerId;
        return dto;
    }
}
