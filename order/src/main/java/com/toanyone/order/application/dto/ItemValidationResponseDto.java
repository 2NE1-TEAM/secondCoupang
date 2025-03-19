package com.toanyone.order.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemValidationResponseDto {

    @NotNull
    private List<ItemResponseDto> items;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemResponseDto {

        @NotNull
        private Long itemId;

        @NotNull
        private String itemName;

        @NotNull
        private int orderedPrice;

        @NotNull
        private int orderedQuantity;

    }

}
