package com.toanyone.payment.application;

import com.toanyone.order.message.PaymentCancelMessage;
import com.toanyone.order.message.PaymentRequestMessage;
import com.toanyone.payment.domain.entity.Payment;
import com.toanyone.payment.message.PaymentCancelSuccessMessage;
import com.toanyone.payment.message.PaymentSuccessMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j(topic = "OrderKafkaConsumer")
@Service
@RequiredArgsConstructor
public class PaymentKafkaConsumer {

    private final PaymentKafkaProducer paymentKafkaProducer;
    private final PaymentService paymentService;


    @KafkaListener(topics = "payment.requested", groupId = "payment")
    public void consumePaymentRequestMessage(ConsumerRecord<String, PaymentRequestMessage> record,
                                          @Header("X-User-Id") Long userId,
                                          @Header("X-User-Roles") String role,
                                          @Header("X-Slack-Id") String slackId) throws IOException {

        PaymentRequestMessage message = record.value();

        log.info("PAYMENT REQUESTED MESSAGE : {}, {}", message.getOrderId(), message.getAmount());

        Payment payment = paymentService.processPaymentRequest(message.getOrderId(), message.getAmount());

        paymentKafkaProducer.sendPaymentSuccessMessage(
                PaymentSuccessMessage.builder()
                        .orderId(message.getOrderId())
                        .paymentId(payment.getId())
                        .paymentStatus("PAYMENT_SUCCESS")
                        .build()
                ,userId, role, slackId);

    }


    @KafkaListener(topics = "payment.cancel", groupId = "payment")
    public void consumePaymentCancelMessage(ConsumerRecord<String, PaymentCancelMessage> record,
                                             @Header("X-User-Id") Long userId,
                                             @Header("X-User-Roles") String role,
                                             @Header("X-Slack-Id") String slackId) throws IOException {

        PaymentCancelMessage message = record.value();

        log.info("PAYMENT CANCEL MESSAGE : {}, {}", message.getOrderId(), message.getPaymentId());

        Payment payment = paymentService.processPaymentCancelRequest(message.getOrderId());

        paymentKafkaProducer.sendPaymentCancelSuccessMessage(
                PaymentCancelSuccessMessage.builder()
                        .orderId(payment.getOrderId())
                        .paymentId(message.getPaymentId())
                        .paymentStatus("CANCELED")
                        .build()
                ,userId, role, slackId);

    }



}