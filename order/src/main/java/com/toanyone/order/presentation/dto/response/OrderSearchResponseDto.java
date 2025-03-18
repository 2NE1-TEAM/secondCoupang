package com.toanyone.order.presentation.dto.response;

import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.entity.OrderItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderSearchResponseDto {

    private Long orderId;
    private Long userId;
    private Long supplyStoreId;
    private Long receiveStoreId;
    private List<OrderItemSearchResponseDto> orderItems;
    private int totalPrice;

    public static OrderSearchResponseDto fromOrder(Order order) {
        return new OrderSearchResponseDto(
                order.getId(),
                order.getUserId(),
                order.getSupplyStoreId(),
                order.getReceiveStoreId(),
                order.getItems().stream()
                        .map(item -> OrderItemSearchResponseDto.builder()
                                .itemId(item.getId())
                                .itemId(item.getItemId())
                                .itemName(item.getItemName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .collect(Collectors.toList()),
                order.getTotalPrice()
        );
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderItemSearchResponseDto {
        private Long itemId;

        private String itemName;

        private int quantity;

        private int price;

    }

}