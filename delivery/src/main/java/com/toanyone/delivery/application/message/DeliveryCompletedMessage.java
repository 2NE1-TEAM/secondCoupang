package com.toanyone.delivery.application.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryCompletedMessage {
    private Long orderId;
    private String deliveryStatus;
}
