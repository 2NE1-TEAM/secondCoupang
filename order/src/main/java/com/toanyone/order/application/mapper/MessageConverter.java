package com.toanyone.order.application.mapper;

import com.toanyone.order.application.dto.message.OrderDeliveryMessage;
import com.toanyone.order.application.dto.message.OrderPaymentMessage;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import org.springframework.stereotype.Component;

@Component
public class MessageConverter {

    public OrderDeliveryMessage toOrderDeliveryMessage(OrderCreateServiceDto orderCreateRequest, Long orderId) {

        return OrderDeliveryMessage.builder()
                .orderId(orderId)
                .receiveStoreId(orderCreateRequest.getReceiveStoreId())
                .supplyStoreId(orderCreateRequest.getSupplyStoreId())
                .recipient(orderCreateRequest.getDeliveryInfo().getRecipient())
                .build();
    }

    public OrderPaymentMessage toOrderPaymentMessage(Long orderId, int amount) {
        return OrderPaymentMessage.builder()
                .orderId(orderId)
                .amount(amount)
                .build();
    }

}
