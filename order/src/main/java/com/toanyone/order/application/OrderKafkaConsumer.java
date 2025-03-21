package com.toanyone.order.application;

import com.toanyone.delivery.message.*;
import com.toanyone.order.common.exception.OrderException;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.message.DeliveryRequestMessage;
import com.toanyone.payment.message.PaymentCancelMessage;
import com.toanyone.payment.message.PaymentFailedMessage;
import com.toanyone.payment.message.PaymentSuccessMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j(topic = "OrderKafkaConsumer")
@Service
@RequiredArgsConstructor
public class OrderKafkaConsumer {

    private final OrderKafkaProducer orderKafkaProducer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderService orderService;

    @KafkaListener(topics = "payment.success", groupId = "payment")
    public void consumePaymentSuccessMessage(ConsumerRecord<String, PaymentSuccessMessage> record,
                                       @Header("X-User-Id") Long userId,
                                       @Header("X-User-Role") String role,
                                       @Header("X-Slack-Id") Long slackId) throws IOException {
        try {
            PaymentSuccessMessage message = record.value();
            DeliveryRequestMessage deliveryMessage = (DeliveryRequestMessage) redisTemplate.opsForValue().get(String.valueOf(message.getOrderId()));

            if (deliveryMessage == null) {
                throw new OrderException.DeliveryNotFoundException();
            }
            redisTemplate.delete(String.valueOf(message.getOrderId()));
            log.info("DELIVERY REQUEST MESSAGE : {}", deliveryMessage.getOrderId(), deliveryMessage.getDeliveryAddress(), deliveryMessage.getRecipient());
            log.info("PAYMENT SUCCESS MESSAGE : {}, {}, {}", message.getOrderId(), message.getPaymentId(), message.getPaymentStatus());
            orderService.updateOrderStatus(message.getOrderId(), message.getPaymentStatus());
            orderKafkaProducer.sendDeliveryMessage(deliveryMessage, userId, role, slackId);
        } catch (Exception e) {
            throw new OrderException.DeliveryRequestFailedException();
        }

    }


    @KafkaListener(topics = "payment.failed", groupId = "payment")
    public void consumePaymentFailedMessage(ConsumerRecord<String, PaymentFailedMessage> record,
                                                 @Header("X-User-Id") Long userId,
                                                 @Header("X-User-Role") String role,
                                                 @Header("X-Slack-Id") Long slackId) throws IOException {
        try {
            PaymentFailedMessage message = record.value();
            log.info("PAYMENT FAILED MESSAGE : {}, {}", message.getPaymentId(), message.getErrorMessage());

            Order order = orderService.findOrderWithItems(message.getOrderId());
            orderService.updateOrderStatus(order.getId(), message.getPaymentStatus());
            orderService.restoreInventory(order);

        } catch (Exception e) {
            throw new OrderException.DeliveryRequestFailedException();
        }

    }


    @KafkaListener(topics = "delivery.success", groupId = "delivery")
    public void consumeDeliverySuccessMessage(ConsumerRecord<String, DeliverySuccessMessage> record,
                                          @Header("X-User-Id") Long userId,
                                          @Header("X-User-Role") String role,
                                          @Header("X-Slack-Id") Long slackId) throws IOException {
        try {
            DeliverySuccessMessage message = record.value();
            orderService.updateOrderStatus(message.getOrderId(), message.getDeliveryStatus());

            //Todo: Slack 메시지 보내기
//            orderService.sendSlackMessage(slackId, "배송 요청이 시작됩니다.");

            log.info("DELIVERY SUCCESS MESSAGE : {}, {}", message.getOrderId(), message.getDeliveryStatus());
        } catch (ClassCastException e){
            log.error("ClassCastException : {}", e.getMessage());
        }
        catch (OrderException.OrderNotFoundException e) {
            log.error("OrderNotFoundException : {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception : {}", e.getMessage());
            throw new OrderException.DeliveryStatusUpdateFailedException();
        }
    }

    @KafkaListener(topics = "delivery.failed", groupId = "delivery")
    public void consumeDeliveryFailedMessage(ConsumerRecord<String, DeliveryFailedMessage> record,
                                       @Header("X-User-Id") Long userId,
                                       @Header("X-User-Role") String role,
                                       @Header("X-Slack-Id") Long slackId) throws IOException {

        try {
            DeliveryFailedMessage message = record.value();
            log.info("DELIVERY FAILED MESSAGE : {}, {}, {}", message.getOrderId(),message.getDeliveryStatus(),message.getErrorMessage());

            Order order = orderService.findOrderWithItems(message.getOrderId());
            orderService.updateOrderStatus(order.getId(), message.getDeliveryStatus());
            orderService.restoreInventory(order);
            PaymentCancelMessage paymentMessage = PaymentCancelMessage.builder()
                    .paymentId(order.getPaymentId()).build();
            orderKafkaProducer.sendPaymentCancelMessage(paymentMessage, userId, role, slackId);

        } catch (Exception e) {
            throw new OrderException.PaymentRequestFailedException();
        }

    }

    @KafkaListener(topics = "delivery.status.updated", groupId = "delivery")
    public void consumeDeliveryStatusUpdatedMessage(ConsumerRecord<String, DeliveryStatusUpdatedMessage> record,
                                             @Header("X-User-Id") Long userId,
                                             @Header("X-User-Role") String role,
                                             @Header("X-Slack-Id") Long slackId) throws IOException {

        try {
            DeliveryStatusUpdatedMessage message = record.value();
            log.info("DELIVERY STATUS UPDATED MESSAGE : {}, {}, {}", message.getOrderId(),message.getDeliveryStatus());

            Order order = orderService.findOrderWithItems(message.getOrderId());
            orderService.updateOrderStatus(order.getId(), message.getDeliveryStatus());

        } catch (Exception e) {
            throw new OrderException.DeliveryStatusUpdateFailedException();
        }

    }


}