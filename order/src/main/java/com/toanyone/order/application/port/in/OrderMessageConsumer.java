package com.toanyone.order.application.port.in;

import com.toanyone.delivery.message.DeliveryCompletedMessage;
import com.toanyone.delivery.message.DeliveryFailedMessage;
import com.toanyone.delivery.message.DeliveryStatusUpdatedMessage;
import com.toanyone.delivery.message.DeliverySuccessMessage;
import com.toanyone.payment.message.PaymentCancelFailedMessage;
import com.toanyone.payment.message.PaymentCancelSuccessMessage;
import com.toanyone.payment.message.PaymentFailedMessage;
import com.toanyone.payment.message.PaymentSuccessMessage;

public interface OrderMessageConsumer {
    void processPaymentSuccess(PaymentSuccessMessage message, Long userId, String role, String slackId);

    void processPaymentFailed(PaymentFailedMessage message, Long userId, String role, String slackId);

    void processPaymentCancelSuccess(PaymentCancelSuccessMessage message, Long userId, String role, String slackId);

    void processPaymentCancelFailed(PaymentCancelFailedMessage message, Long userId, String role, String slackId);

    void processDeliverySuccess(DeliverySuccessMessage message, Long userId, String role, String slackId);

    void processDeliveryFailed(DeliveryFailedMessage message, Long userId, String role, String slackId);

    void processDeliveryStatusUpdated(DeliveryStatusUpdatedMessage message, Long userId, String role, String slackId);

    void processDeliveryCompleted(DeliveryCompletedMessage message, Long userId, String role, String slackId);


}
