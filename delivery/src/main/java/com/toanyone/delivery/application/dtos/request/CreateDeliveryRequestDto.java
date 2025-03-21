package com.toanyone.delivery.application.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateDeliveryRequestDto {
    @NotNull
    @JsonProperty("delivery_hub_id")
    private Long deliveryHubId;
    @NotNull
    @JsonProperty("arrival_hub_id")
    private Long arrivalHubId;
    @NotNull
    @JsonProperty("delivery_address")
    private String deliveryAddress;
    @NotNull
    @JsonProperty("order_id")
    private Long orderId;
    @NotNull
    private String recipient;
    @JsonProperty("recipient_slack_id")
    @NotNull
    private String recipientSlackId;

}
