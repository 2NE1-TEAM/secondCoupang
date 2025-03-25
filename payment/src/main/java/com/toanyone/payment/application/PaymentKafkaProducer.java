package com.toanyone.payment.application;

import com.toanyone.payment.message.PaymentCancelFailedMessage;
import com.toanyone.payment.message.PaymentCancelSuccessMessage;
import com.toanyone.payment.message.PaymentFailedMessage;
import com.toanyone.payment.message.PaymentSuccessMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class PaymentKafkaProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentSuccessMessage(PaymentSuccessMessage message, Long userId, String role, String slackId) {
        Message<PaymentSuccessMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "payment.success")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Roles", role)
                .setHeader("X-Slack-Id", slackId)
                .build();
        kafkaTemplate.send(kafkaMessage);
    }

    public void sendPaymentFailedMessage(PaymentFailedMessage message, Long userId, String role, String slackId) {
        Message<PaymentFailedMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "payment.failed")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Roles", role)
                .setHeader("X-Slack-Id", slackId)
                .build();
        kafkaTemplate.send(kafkaMessage);
    }



    public void sendPaymentCancelSuccessMessage(PaymentCancelSuccessMessage message, Long userId, String role, String slackId) {
        Message<PaymentCancelSuccessMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "payment.cancel.success")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Roles", role)
                .setHeader("X-Slack-Id", slackId)
                .build();
        kafkaTemplate.send(kafkaMessage);
    }

    public void sendPaymentCancelFailedMessage(PaymentCancelFailedMessage message, Long userId, String role, String slackId) {
        Message<PaymentCancelFailedMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "payment.cancel.failed")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Roles", role)
                .setHeader("X-Slack-Id", slackId)
                .build();
        kafkaTemplate.send(kafkaMessage);
    }


}
