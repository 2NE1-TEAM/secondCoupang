package com.toanyone.item.presentation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemUpdateRequestDto {
    @Size(max = 30)
    private String itemName;
    private Integer price;
    private Integer stock;
    private String imageUrl;
}
