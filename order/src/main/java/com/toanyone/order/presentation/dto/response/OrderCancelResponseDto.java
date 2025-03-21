package com.toanyone.order.presentation.dto.response;

import com.toanyone.order.domain.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCancelResponseDto {

    @NotNull
    private Long orderId;

    @NotBlank
    private String orderStatus;

    public static OrderCancelResponseDto fromOrder(Order order) {
        return new OrderCancelResponseDto(order.getId(), order.getStatus().getDescription());
    }

}