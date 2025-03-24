package com.toanyone.item.infrastructure.repository;

import com.toanyone.item.presentation.dto.CursorPage;
import com.toanyone.item.presentation.dto.ItemSearchRequest;

public interface ItemRepositoryCustom {
    CursorPage search(ItemSearchRequest itemSearchRequest, String sortBy, String direction, int size);
}
