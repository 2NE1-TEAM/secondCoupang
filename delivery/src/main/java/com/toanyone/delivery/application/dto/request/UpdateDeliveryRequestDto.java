package com.toanyone.delivery.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateDeliveryRequestDto {
    @JsonProperty("delivery_status")
    @NotNull
    private String deliveryStatus;
    private String recipient;
    @JsonProperty("recipient_slack_id")
    private String recipientSlackId;
    @JsonProperty("delivery_address")
    private String deliveryAddress;
}
