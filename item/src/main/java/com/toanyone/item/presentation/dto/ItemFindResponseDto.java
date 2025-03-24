package com.toanyone.item.presentation.dto;

import com.toanyone.item.domain.model.Item;
import lombok.*;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemFindResponseDto implements Serializable {
    private Long itemId;
    private String itemName;
    private Integer price;
    private Integer stock;
    private Long storeId;

    public static ItemFindResponseDto of(Item item) {
        ItemFindResponseDto itemFindResponseDto = new ItemFindResponseDto();
        itemFindResponseDto.itemId = item.getId();
        itemFindResponseDto.itemName = item.getItemName();
        itemFindResponseDto.storeId = item.getStoreId();
        itemFindResponseDto.price = item.getPrice();
        itemFindResponseDto.stock = item.getStock();
        return itemFindResponseDto;
    }
}
