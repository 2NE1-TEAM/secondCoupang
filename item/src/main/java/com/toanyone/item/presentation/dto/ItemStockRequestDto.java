package com.toanyone.item.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@NotNull
public class ItemStockRequestDto {
    @NotNull
    private Long itemId;

    @NotNull
    private Integer quantity;

    @NotNull
    private AdjustmentType type;
}
