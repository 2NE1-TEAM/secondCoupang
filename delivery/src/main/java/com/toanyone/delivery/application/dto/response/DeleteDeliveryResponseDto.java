package com.toanyone.delivery.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.toanyone.delivery.domain.Delivery;
import lombok.Getter;

@Getter
public class DeleteDeliveryResponseDto {
    @JsonProperty("deleted_delivery_id")
    private long deletedDeliveryId;

    public static DeleteDeliveryResponseDto from(Delivery deletedDelivery) {
        DeleteDeliveryResponseDto dto = new DeleteDeliveryResponseDto();
        dto.deletedDeliveryId = deletedDelivery.getId();
        return dto;
    }
}
