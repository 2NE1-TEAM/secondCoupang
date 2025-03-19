package com.toanyone.order.application;

import com.toanyone.order.application.dto.message.OrderDeliveryMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class OrderKafkaProducer {

    @Autowired
    private KafkaTemplate<String, OrderDeliveryMessage> orderDeliveryKafkaTemplate;

//    @Autowired
//    private KafkaTemplate<String, OrderDeliveryMessage> paymentKafkaTemplate;

    public void sendOrderDeliveryMessage(OrderDeliveryMessage message, Long userId, String role, Long slackId) {
        Message<OrderDeliveryMessage> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, "order-delivery")
                .setHeader("X-User-Id", userId)
                .setHeader("X-User-Role", role)
                .setHeader("X-Slack-Id", slackId)
                .build();

        orderDeliveryKafkaTemplate.send(kafkaMessage);
    }

}
