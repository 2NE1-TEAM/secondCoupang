package com.toanyone.delivery.application.dtos.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryCompletedMessage {
    private Long orderId;
    private String deliveryStatus;
}
