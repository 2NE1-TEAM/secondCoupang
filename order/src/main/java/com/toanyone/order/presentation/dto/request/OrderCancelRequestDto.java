package com.toanyone.order.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCancelRequestDto {

    @NotNull
    private Long orderId;

    @NotNull
    private Long deliveryId;

}