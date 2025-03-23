package com.toanyone.order.message;

import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequestMessage {
    private Long orderId;
    private String ordererName;
    private Long supplyStoreId;
    private Long receiveStoreId;
    private Long arrivalHubId;
    private Long departureHubId;
    private String request;
    private List<OrderItemRequestMessage> items;
    private String deliveryAddress;
    private String recipient;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItemRequestMessage{
        private String itemName;
        private int quantity;
    }


}
