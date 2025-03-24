package com.toanyone.order.application;

import com.toanyone.order.message.DeliveryRequestMessage;
import com.toanyone.order.message.PaymentCancelMessage;
import com.toanyone.order.message.PaymentRequestMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentRequestMessage(PaymentRequestMessage message, Long userId, String role, String slackId) {
        Message<PaymentRequestMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "payment.requested")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Role", role)
                .setHeader("X-Slack-Id", slackId)
                .build();
        kafkaTemplate.send(kafkaMessage);
    }

    public void sendPaymentCancelMessage(PaymentCancelMessage message, Long userId, String role, String slackId) {
        Message<PaymentCancelMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "payment.cancel")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Role", role)
                .setHeader("X-Slack-Id", slackId)
                .build();
        kafkaTemplate.send(kafkaMessage);
    }


    public void sendDeliveryRequestMessage(DeliveryRequestMessage message, Long userId, String role, String slackId) {
        Message<DeliveryRequestMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.GROUP_ID, "delivery")
                .setHeader(KafkaHeaders.TOPIC, "delivery.requested")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Role", role)
                .setHeader("X-Slack-Id", slackId)
                .build();
        kafkaTemplate.send(kafkaMessage);
    }


}
