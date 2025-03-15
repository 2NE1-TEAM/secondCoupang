package com.toanyone.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemValidationResponseDto {

    private List<ItemResponseDto> items;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemResponseDto {

        private Long itemId;

        private String itemName;

        private int orderedPrice;

        private int orderedQuantity;

    }

}
