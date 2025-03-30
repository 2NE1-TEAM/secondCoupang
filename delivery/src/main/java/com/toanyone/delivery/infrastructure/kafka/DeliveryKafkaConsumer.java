package com.toanyone.delivery.infrastructure.kafka;

import com.toanyone.delivery.application.DeliveryService;
import com.toanyone.delivery.application.dtos.request.DeliveryRequestMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@KafkaListener(topics = "delivery.requested", groupId = "delivery")
@RequiredArgsConstructor
public class DeliveryKafkaConsumer {
    private final DeliveryService deliveryService;

    public void consume(DeliveryRequestMessage message,
                        @Header("X-User-Id") Long userId,
                        @Header("X-User-Roles") String userRole,
                        @Header("X-Slack-Id") String slackId) throws IOException {
        deliveryService.createDelivery(message, userId, userRole, slackId);
    }
}
