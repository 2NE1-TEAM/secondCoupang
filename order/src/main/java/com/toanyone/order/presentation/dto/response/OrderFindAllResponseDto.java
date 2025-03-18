package com.toanyone.order.presentation.dto.response;

import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.entity.OrderItem;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderFindAllResponseDto {

    @NotNull
    private Long orderId;

    @NotNull
    private Long supplyStoreId;

    @NotNull
    private Long receiveStoreId;

    @NotNull
    private List<Long> orderItemIds;
    @NotNull
    private int totalPrice;

    public static OrderFindAllResponseDto fromOrder(Order order) {
        OrderFindAllResponseDto responseDto = new OrderFindAllResponseDto();
        responseDto.orderId = order.getId();
        responseDto.supplyStoreId = order.getSupplyStoreId();
        responseDto.receiveStoreId = order.getReceiveStoreId();
        responseDto.orderItemIds = order.getItems().stream().map(OrderItem::getItemId).collect(Collectors.toList());
        responseDto.totalPrice = order.getTotalPrice();
        return responseDto;
    }

}
