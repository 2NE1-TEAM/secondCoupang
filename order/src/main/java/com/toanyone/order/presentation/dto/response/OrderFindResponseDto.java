package com.toanyone.order.presentation.dto.response;

import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.entity.OrderItem;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @NotNull
    private List<Long> orderItemIds;

    @NotNull
    private int totalPrice;

    public static OrderFindResponseDto fromOrder(Order order) {
        OrderFindResponseDto responseDto = new OrderFindResponseDto();
        responseDto.orderId = order.getId();
        responseDto.userId = order.getUserId();
        responseDto.supplyStoreId = order.getSupplyStoreId();
        responseDto.receiveStoreId = order.getReceiveStoreId();
        responseDto.orderItemIds = order.getItems().stream()
                .map(OrderItem::getId).collect(Collectors.toList());
        responseDto.totalPrice = order.getTotalPrice();
        return responseDto;
    }

}
