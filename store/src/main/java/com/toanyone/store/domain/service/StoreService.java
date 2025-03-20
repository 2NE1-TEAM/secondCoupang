package com.toanyone.store.domain.service;

import com.toanyone.store.presentation.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreService {
    StoreCreateResponseDto createStore(StoreCreateRequestDto storeCreateRequestDto);

    StoreFindResponseDto findOne(Long storeId);

    void deleteStore(Long storeId);

    void updateStore(Long storeId, StoreUpdateRequestDto requestDto);

    CursorPage<StoreFindResponseDto> findStores(StoreSearchRequest storeSearchRequest, String sortBy, String direction, int size);
}
