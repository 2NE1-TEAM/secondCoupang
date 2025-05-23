package com.toanyone.payment.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelFailedMessage {
    private Long paymentId;
    private Long orderId;
    private String paymentStatus;
    private String errorMessage;
}
