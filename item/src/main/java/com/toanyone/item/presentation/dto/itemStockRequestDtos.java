package com.toanyone.item.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class itemStockRequestDtos {
    private AdjustmentType type;
    List<ItemStockRequestDto> items;
}
