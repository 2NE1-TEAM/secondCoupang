package com.toanyone.order.infrastructure.kafka;

import com.toanyone.delivery.message.DeliveryCompletedMessage;
import com.toanyone.delivery.message.DeliveryFailedMessage;
import com.toanyone.delivery.message.DeliveryStatusUpdatedMessage;
import com.toanyone.delivery.message.DeliverySuccessMessage;
import com.toanyone.order.application.dto.SlackMessageRequestDto;
import com.toanyone.order.application.service.AiService;
import com.toanyone.order.application.service.OrderMessageConsumer;
import com.toanyone.order.application.service.OrderService;
import com.toanyone.order.common.config.UserContext;
import com.toanyone.order.common.exception.OrderException;
import com.toanyone.order.infrastructure.kafka.OrderKafkaProducer;
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
// implements OrderMessageConsumer

    private final OrderKafkaProducer orderKafkaProducer;
    private final OrderService orderService;
    private final AiService aiService;

    @KafkaListener(topics = "payment.success", groupId = "payment")
    public void consumePaymentSuccessMessage(ConsumerRecord<String, PaymentSuccessMessage> record,
                                       @Header("X-User-Id") Long userId,
                                       @Header("X-User-Roles") String role,
                                       @Header("X-Slack-Id") String slackId) throws IOException {
        try {

            PaymentSuccessMessage message = record.value();

            sendDeliveryRequestAfterPayment(message, userId, role, slackId);

            UserContext context = UserContext.builder()
                    .userId(userId)
                    .role(role)
                    .slackId(slackId)
                    .build();

            UserContext.setUserContext(context);

            sendSlackMessageAfterProcessDeliveryRequest(message, slackId);

            UserContext.clear();

        } catch (Exception e) {
            log.error("PAYMENT SUCCESS EXCEPTION", e);
            throw new OrderException.DeliveryRequestFailedException();
        }

    }

    private void sendSlackMessageAfterProcessDeliveryRequest(PaymentSuccessMessage message, String slackId) {
        SlackMessageRequestDto slackMessage = SlackMessageRequestDto.builder()
                .slackId(slackId)
                .orderId(message.getOrderId())
                .message("주문의 결제가 완료되었습니다.").build();

        aiService.sendSlackMessage(slackMessage);
    }

    private void sendDeliveryRequestAfterPayment(PaymentSuccessMessage message, Long userId, String role, String slackId) {
        DeliveryRequestMessage deliveryMessage = orderService.processDeliveryRequest(message.getOrderId(), message.getPaymentStatus());
        orderKafkaProducer.sendDeliveryRequestMessage(deliveryMessage, userId, role, slackId);
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

            UserContext context = UserContext.builder()
                    .userId(userId)
                    .role(role)
                    .slackId(slackId)
                    .build();

            UserContext.setUserContext(context);

            SlackMessageRequestDto slackMessage = SlackMessageRequestDto.builder()
                    .slackId(slackId)
                    .orderId(message.getOrderId())
                    .message("주문의 결제가 실패했습니다.").build();

            aiService.sendSlackMessage(slackMessage);

        } catch (Exception e) {
            log.error("PAYMENT FAILED EXCEPTION", e);
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
            log.error("PAYMENT CANCEL SUCCESS EXCEPTION", e);
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

        } catch (Exception e) {
            log.error("PAYMENT CANCEL FAILED EXCEPTION", e);
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

            log.info("DELIVERY SUCCESS MESSAGE : {}, {}", message.getOrderId(), message.getDeliveryStatus());
        } catch (ClassCastException e){
            log.error("ClassCastException : {}", e.getMessage());
        } catch (OrderException.OrderNotFoundException e) {
            log.error("OrderNotFoundException : {}", e.getMessage());
        } catch (Exception e) {
            log.error("DELIVERY SUCCESS EXCEPTION", e);
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

            UserContext context = UserContext.builder()
                    .userId(userId)
                    .role(role)
                    .slackId(slackId)
                    .build();

            UserContext.setUserContext(context);

            SlackMessageRequestDto slackMessage = SlackMessageRequestDto.builder()
                    .slackId(slackId)
                    .orderId(message.getOrderId())
                    .message("주문이 실패했습니다.").build();

            aiService.sendSlackMessage(slackMessage);


        } catch (Exception e) {
            log.error("DELIVERY FAILED EXCEPTION", e);
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
            log.error("DELIVERY STATUS UPDATED EXCEPTION", e);
            throw new OrderException.DeliveryStatusUpdateFailedException();
        }

    }

    @KafkaListener(topics = "delivery.completed", groupId = "delivery")
    public void consumeDeliveryCompletedMessage(ConsumerRecord<String, DeliveryCompletedMessage> record,
                                                    @Header("X-User-Id") Long userId,
                                                    @Header("X-User-Roles") String role,
                                                    @Header("X-Slack-Id") String slackId) throws IOException {

        try {
            DeliveryCompletedMessage message = record.value();
            log.info("DELIVERY COMPLETED MESSAGE : {}, {}", message.getOrderId(),message.getDeliveryStatus());
            orderService.processDeliveryUpdatedRequest(message.getOrderId(), message.getDeliveryStatus());

        } catch (Exception e) {
            log.error("DELIVERY COMPLETED EXCEPTION", e);
            throw new OrderException.DeliveryStatusUpdateFailedException();
        }

    }

}