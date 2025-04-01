package com.toanyone.delivery.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.toanyone.delivery.domain.Delivery;
import lombok.Getter;

@Getter
public class UpdateDeliveryResponseDto {
    @JsonProperty("updated_delivery_id")
    private Long updatedDeliveryId;

    public static UpdateDeliveryResponseDto from(Delivery delivery) {
        UpdateDeliveryResponseDto response = new UpdateDeliveryResponseDto();
        response.updatedDeliveryId = delivery.getId();
        return response;
    }
}
