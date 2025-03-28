package com.toanyone.order.application.dto.service.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class OrderCancelServiceDto {
    private Long orderId;
}