package com.toanyone.order.application.service;

import com.toanyone.order.message.DeliveryRequestMessage;
import com.toanyone.order.message.PaymentCancelMessage;
import com.toanyone.order.message.PaymentRequestMessage;

public interface OrderMessageProducer {

    void sendPaymentRequestMessage(PaymentRequestMessage message, Long userId, String role, String slackId);

    void sendPaymentCancelMessage(PaymentCancelMessage message, Long userId, String role, String slackId);

    void sendDeliveryRequestMessage(DeliveryRequestMessage message, Long userId, String role, String slackId);

}


