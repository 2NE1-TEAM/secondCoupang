package com.toanyone.delivery.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CreateDeliveryResponseDto {
    @JsonProperty("delivery_id")
    private Long deliveryId;

    public static CreateDeliveryResponseDto from(Long deliveryId) {
        CreateDeliveryResponseDto dto = new CreateDeliveryResponseDto();
        dto.deliveryId = deliveryId;
        return dto;
    }
}
