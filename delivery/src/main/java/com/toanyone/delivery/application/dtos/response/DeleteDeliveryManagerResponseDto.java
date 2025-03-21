package com.toanyone.delivery.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.toanyone.delivery.domain.DeliveryManager;
import lombok.Getter;

@Getter
public class DeleteDeliveryManagerResponseDto {
    @JsonProperty("delivery_manager_id")
    private Long deliveryManagerId;

    public static DeleteDeliveryManagerResponseDto from(DeliveryManager deliveryManager) {
        DeleteDeliveryManagerResponseDto responseDto = new DeleteDeliveryManagerResponseDto();
        responseDto.deliveryManagerId = deliveryManager.getId();
        return responseDto;
    }
}
