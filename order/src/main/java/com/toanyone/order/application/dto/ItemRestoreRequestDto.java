package com.toanyone.order.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemRestoreRequestDto {

    @NotNull
    private List<ItemRequestDto> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemRequestDto {

        @NotNull
        private Long itemId;

        @NotNull
        private int quantity;
    }
}
