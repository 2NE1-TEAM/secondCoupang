package com.toanyone.order.application;

import com.toanyone.order.application.dto.message.OrderDeliveryMessage;
import com.toanyone.order.application.dto.message.OrderPaymentMessage;
import org.apache.kafka.common.header.internals.RecordHeader;
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

    public void sendDeliveryMessage(OrderDeliveryMessage message, Long userId, String role, Long slackId) {
        Message<OrderDeliveryMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.GROUP_ID, "delivery-group")
                .setHeader(KafkaHeaders.TOPIC, "order-delivery")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Role", role)
                .setHeader("X-Slack-Id", slackId)
                .build();
        kafkaTemplate.send(kafkaMessage);
    }


    public void sendPaymentMessage(OrderPaymentMessage message, Long userId, String role, Long slackId) {
        Message<OrderPaymentMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.GROUP_ID, "payment-group")
                .setHeader(KafkaHeaders.TOPIC, "order-payment")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Role", role)
                .setHeader("X-Slack-Id", slackId)
                .build();
        kafkaTemplate.send(kafkaMessage);
    }

}
