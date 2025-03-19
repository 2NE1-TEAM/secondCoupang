package com.toanyone.order.application.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderMessage {
    private Long paymentId;
    private Long orderId;
    private String paymentStatus;
}
