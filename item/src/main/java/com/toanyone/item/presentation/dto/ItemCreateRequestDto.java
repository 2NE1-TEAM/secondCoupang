package com.toanyone.item.presentation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemCreateRequestDto {
    @NotEmpty
    private String itemName;
    @NotNull
    private Integer stock;
    @NotNull
    private Integer price;

    private String imageUrl;

    @NotNull
    private Long storeId;
}
