package com.toanyone.order.application.service;

import com.toanyone.delivery.message.DeliveryFailedMessage;
import com.toanyone.delivery.message.DeliveryStatusUpdatedMessage;
import com.toanyone.delivery.message.DeliverySuccessMessage;
import com.toanyone.order.common.exception.OrderException;
import com.toanyone.order.message.DeliveryRequestMessage;
import com.toanyone.order.message.PaymentCancelMessage;
import com.toanyone.payment.message.PaymentCancelFailedMessage;
import com.toanyone.payment.message.PaymentCancelSuccessMessage;
import com.toanyone.payment.message.PaymentFailedMessage;
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
public class OrderKafkaConsumer {

    private final OrderKafkaProducer orderKafkaProducer;
    private final OrderService orderService;
//    private final AiService aiService;

    @KafkaListener(topics = "payment.success", groupId = "payment")
    public void consumePaymentSuccessMessage(ConsumerRecord<String, PaymentSuccessMessage> record,
                                       @Header("X-User-Id") Long userId,
                                       @Header("X-User-Roles") String role,
                                       @Header("X-Slack-Id") String slackId) throws IOException {
        try {
            PaymentSuccessMessage message = record.value();
            DeliveryRequestMessage deliveryMessage = orderService.processDeliveryRequest(message.getOrderId(), message.getPaymentStatus());
            orderKafkaProducer.sendDeliveryRequestMessage(deliveryMessage, userId, role, slackId);
        } catch (Exception e) {
            throw new OrderException.DeliveryRequestFailedException();
        }

    }


    @KafkaListener(topics = "payment.failed", groupId = "payment")
    public void consumePaymentFailedMessage(ConsumerRecord<String, PaymentFailedMessage> record,
                                                 @Header("X-User-Id") Long userId,
                                                 @Header("X-User-Roles") String role,
                                                 @Header("X-Slack-Id") String slackId) throws IOException {
        try {
            PaymentFailedMessage message = record.value();
            log.info("PAYMENT FAILED MESSAGE : {}, {}", message.getPaymentStatus(), message.getErrorMessage());
            orderService.processOrderCancellation(message.getOrderId(),message.getPaymentStatus());

        } catch (Exception e) {
            throw new OrderException.OrderCancelFailedException();
        }

    }

    @KafkaListener(topics = "payment.cancel.success", groupId = "payment")
    public void consumePaymentCancelSuccessMessage(ConsumerRecord<String, PaymentCancelSuccessMessage> record,
                                            @Header("X-User-Id") Long userId,
                                            @Header("X-User-Roles") String role,
                                            @Header("X-Slack-Id") String slackId) throws IOException {
        try {
            PaymentCancelSuccessMessage message = record.value();

            log.info("PAYMENT CANCEL SUCCESS MESSAGE : {}, {}", message.getPaymentId(), message.getPaymentStatus());
            orderService.processOrderCancellation(message.getOrderId(), message.getPaymentStatus());
        } catch (Exception e) {
            throw new OrderException.OrderCancelFailedException();
        }

    }

    @KafkaListener(topics = "payment.cancel.failed", groupId = "payment")
    public void consumePaymentCancelFailedMessage(ConsumerRecord<String, PaymentCancelFailedMessage> record,
                                                   @Header("X-User-Id") Long userId,
                                                   @Header("X-User-Roles") String role,
                                                   @Header("X-Slack-Id") String slackId) throws IOException {
        try {
            PaymentCancelFailedMessage message = record.value();

            log.info("PAYMENT CANCEL FAILED MESSAGE : {}, {}", message.getPaymentId(), message.getErrorMessage());
//            aiService.sendSlackMessage(slackId, "결제 취소가 실패했습니다.");
        } catch (Exception e) {
            throw new OrderException.OrderCancelFailedException();
        }

    }


    @KafkaListener(topics = "delivery.success", groupId = "delivery")
    public void consumeDeliverySuccessMessage(ConsumerRecord<String, DeliverySuccessMessage> record,
                                          @Header("X-User-Id") Long userId,
                                          @Header("X-User-Roles") String role,
                                          @Header("X-Slack-Id") String slackId) throws IOException {
        try {
            DeliverySuccessMessage message = record.value();

            orderService.processDeliverySuccessRequest(message.getOrderId(), message.getDeliveryStatus());

//            aiService.sendSlackMessage(slackId, "배송 요청이 시작됩니다.");

            log.info("DELIVERY SUCCESS MESSAGE : {}, {}", message.getOrderId(), message.getDeliveryStatus());
        } catch (ClassCastException e){
            log.error("ClassCastException : {}", e.getMessage());
        } catch (OrderException.OrderNotFoundException e) {
            log.error("OrderNotFoundException : {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception : {}", e.getMessage());
            throw new OrderException.DeliveryStatusUpdateFailedException();
        }
    }

    @KafkaListener(topics = "delivery.failed", groupId = "delivery")
    public void consumeDeliveryFailedMessage(ConsumerRecord<String, DeliveryFailedMessage> record,
                                       @Header("X-User-Id") Long userId,
                                       @Header("X-User-Roles") String role,
                                       @Header("X-Slack-Id") String slackId) throws IOException {

        try {
            DeliveryFailedMessage message = record.value();
            log.info("DELIVERY FAILED MESSAGE : {}, {}, {}", message.getOrderId(),message.getDeliveryStatus(),message.getErrorMessage());
            PaymentCancelMessage paymentMessage = orderService.processDeliveryFailedRequest(message.getOrderId(), message.getDeliveryStatus());
            orderKafkaProducer.sendPaymentCancelMessage(paymentMessage, userId, role, slackId);

        } catch (Exception e) {
            throw new OrderException.PaymentRequestFailedException();
        }
    }



    @KafkaListener(topics = "delivery.status.updated", groupId = "delivery")
    public void consumeDeliveryStatusUpdatedMessage(ConsumerRecord<String, DeliveryStatusUpdatedMessage> record,
                                             @Header("X-User-Id") Long userId,
                                             @Header("X-User-Roles") String role,
                                             @Header("X-Slack-Id") String slackId) throws IOException {

        try {
            DeliveryStatusUpdatedMessage message = record.value();
            log.info("DELIVERY STATUS UPDATED MESSAGE : {}, {}", message.getOrderId(),message.getDeliveryStatus());
            orderService.processDeliveryUpdatedRequest(message.getOrderId(), message.getDeliveryStatus());

        } catch (Exception e) {
            throw new OrderException.DeliveryStatusUpdateFailedException();
        }

    }

}