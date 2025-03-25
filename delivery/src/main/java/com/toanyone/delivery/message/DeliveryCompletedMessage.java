package com.toanyone.delivery.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryCompletedMessage {
    private Long orderId;
    private String deliveryStatus;
}
