package com.toanyone.order.application.mapper;

import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.message.DeliveryRequestMessage;
import com.toanyone.order.message.PaymentRequestMessage;
import org.springframework.stereotype.Component;

@Component
public class MessageConverter {

    public DeliveryRequestMessage toOrderDeliveryMessage(OrderCreateServiceDto orderCreateRequest, Long orderId) {

        return DeliveryRequestMessage.builder()
                .orderId(orderId)
                .receiveStoreId(orderCreateRequest.getReceiveStoreId())
                .supplyStoreId(orderCreateRequest.getSupplyStoreId())
                .recipient(orderCreateRequest.getDeliveryInfo().getRecipient())
                .build();
    }

    public PaymentRequestMessage toOrderPaymentMessage(Long orderId, int amount) {
        return PaymentRequestMessage.builder()
                .orderId(orderId)
                .amount(amount)
                .build();
    }

}
