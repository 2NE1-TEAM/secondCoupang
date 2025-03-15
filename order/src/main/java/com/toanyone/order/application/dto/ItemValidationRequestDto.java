package com.toanyone.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemValidationRequestDto {

    private List<ItemRequestDto> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemRequestDto {
        private Long itemId;
        private int quantity;
    }
}
