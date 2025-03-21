package com.toanyone.order.application.mapper;

import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.message.DeliveryRequestMessage;
import com.toanyone.order.message.PaymentRequestMessage;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MessageConverter {

    public DeliveryRequestMessage toOrderDeliveryMessage(OrderCreateServiceDto orderCreateRequest, Long orderId, Long arrivalHubId, Long departureHubId) {

        return DeliveryRequestMessage.builder()
                .orderId(orderId)
                .ordererName(orderCreateRequest.getOrdererName())
                .arrivalHubId(arrivalHubId)
                .departureHubId(departureHubId)
                .receiveStoreId(orderCreateRequest.getReceiveStoreId())
                .supplyStoreId(orderCreateRequest.getSupplyStoreId())
                .request(orderCreateRequest.getRequest())
                .items(orderCreateRequest.getItems().stream()
                        .map(item -> DeliveryRequestMessage.OrderItemRequestMessage.builder()
                                .itemName(item.getItemName())
                                .quantity(item.getQuantity())
                                .build()
                ).collect(Collectors.toList()))
                .deliveryAddress(orderCreateRequest.getDeliveryInfo().getDeliveryAddress())
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
