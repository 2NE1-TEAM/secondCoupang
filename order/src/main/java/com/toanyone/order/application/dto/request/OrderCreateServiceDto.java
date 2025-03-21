package com.toanyone.order.application.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
public class OrderCreateServiceDto {
    private Long userId;
    private Long supplyStoreId;
    private Long receiveStoreId;
    private List<OrderCreateServiceDto.ItemRequestDto> items;
    private OrderCreateServiceDto.DeliveryRequestDto deliveryInfo;

    @Getter
    @Builder
    public static class ItemRequestDto {
        private Long itemId;
        private String itemName;
        private int price;
        private int quantity;
    }

    @Getter
    @Builder
    public static class DeliveryRequestDto {
        private String recipient;
    }

}

