package com.toanyone.payment.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessMessage {
    private Long paymentId;
    private Long orderId;
    private String paymentStatus;
}
