package com.toanyone.store.infrastructure.repository;

import com.toanyone.store.presentation.dto.CursorPage;
import com.toanyone.store.presentation.dto.StoreFindResponseDto;
import com.toanyone.store.presentation.dto.StoreSearchRequest;

public interface StoreRepositoryCustom {
    CursorPage<StoreFindResponseDto> search(StoreSearchRequest storeSearchRequest, String sortBy, String direction, int size);
}
