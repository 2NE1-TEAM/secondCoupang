package com.toanyone.order.application;

import com.toanyone.order.application.dto.message.OrderDeliveryMessage;
import com.toanyone.order.application.dto.message.OrderPaymentMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j(topic = "OrderKafkaConsumer")
@Service
public class OrderKafkaConsumer {

    @KafkaListener(topics = "order-delivery", groupId = "delivery-group") //topics = "delivery-order" 변경
    public void consumeDeliveryMessage(ConsumerRecord<String, OrderDeliveryMessage> record, //DeliveryOrderMessage 변경
                                       @Header("X-User-Id") Long userId,
                                       @Header("X-User-Role") String role,
                                       @Header("X-Slack-Id") Long slackId) throws IOException {

        OrderDeliveryMessage message = record.value();

        log.info("DELIVERY MESSAGE : {}, {}, {}", message.getRecipient(),message.getSupplyStoreId(),message.getReceiveStoreId());

    }

    @KafkaListener(topics = "order-payment", groupId = "payment-group") //topics = "payment-order" 변경
    public void consumePaymentMessage(ConsumerRecord<String, OrderPaymentMessage> record,  //PaymentOrderMessage 변경
                                       @Header("X-User-Id") Long userId,
                                       @Header("X-User-Role") String role,
                                       @Header("X-Slack-Id") Long slackId) throws IOException {

        OrderPaymentMessage message = record.value();

        log.info("PAYMENT MESSAGE : {}, {}", message.getOrderId(), message.getAmount());

    }


}