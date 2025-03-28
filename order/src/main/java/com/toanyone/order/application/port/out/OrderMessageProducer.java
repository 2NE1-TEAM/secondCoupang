package com.toanyone.order.application.port.out;

import com.toanyone.order.application.dto.message.DeliveryRequestMessage;
import com.toanyone.order.application.dto.message.PaymentCancelMessage;
import com.toanyone.order.application.dto.message.PaymentRequestMessage;

public interface OrderMessageProducer {

    void sendPaymentRequestMessage(PaymentRequestMessage message, Long userId, String role, String slackId);

    void sendPaymentCancelMessage(PaymentCancelMessage message, Long userId, String role, String slackId);

    void sendDeliveryRequestMessage(DeliveryRequestMessage message, Long userId, String role, String slackId);

}


