package com.toanyone.delivery.application.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryManagerRequestDto {
    @JsonProperty("delivery_manager_type")
    private String deliveryManagerType;
    @JsonProperty("hub_id")
    private Long hubId;
    @JsonProperty("delivery_order")
    private Long deliveryOrder;
    @JsonProperty("user_id")
    private Long userId;
}
