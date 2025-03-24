package com.toanyone.item.domain.service;


import com.toanyone.item.presentation.dto.*;

import java.util.List;

public interface ItemService {
    ItemCreateResponseDto createItem(ItemCreateRequestDto storeCreateRequestDto);

    ItemFindResponseDto findOne(Long storeId);

    void deleteItem(Long storeId);

    ItemUpdateResponseDto updateItem(Long storeId, ItemUpdateRequestDto requestDto);

    CursorPage<ItemFindResponseDto> findItems(ItemSearchRequest itemSearchRequest, String sortBy, String direction, int size);

    void adjustStock(itemStockRequestDtos requestDtos);
}
