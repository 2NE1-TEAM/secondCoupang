package com.toanyone.delivery.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCompletedMessage {
    private Long orderId;
    private String deliveryStatus;
}