package com.toanyone.order.application.dto.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryOrderMessage {
    private Long orderId;
    private String deliveryStatus;
}