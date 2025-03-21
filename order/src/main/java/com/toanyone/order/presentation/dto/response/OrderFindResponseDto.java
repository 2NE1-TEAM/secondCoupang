package com.toanyone.order.presentation.dto.response;

import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.entity.OrderItem;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFindResponseDto {

    @NotNull
    private Long orderId;

    @NotNull
    private Long userId;

    @NotNull
    private Long supplyStoreId;

    @NotNull
    private Long receiveStoreId;

//    @NotNull
//    private List<Long> orderItemIds;

    @NotNull
    private List<OrderItemResponseDto> items;

    @NotNull
    private int totalPrice;

    public static OrderFindResponseDto fromOrder(Order order) {
        OrderFindResponseDto responseDto = new OrderFindResponseDto();
        responseDto.orderId = order.getId();
        responseDto.userId = order.getUserId();
        responseDto.supplyStoreId = order.getSupplyStoreId();
        responseDto.receiveStoreId = order.getReceiveStoreId();
        responseDto.items = order.getItems().stream()
                .map(item -> OrderFindResponseDto.OrderItemResponseDto.builder()
                        .itemId(item.getId())
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());
        responseDto.totalPrice = order.getTotalPrice();
        return responseDto;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderItemResponseDto {
        private Long itemId;

        private String itemName;

        private int quantity;

        private int price;

    }

}
