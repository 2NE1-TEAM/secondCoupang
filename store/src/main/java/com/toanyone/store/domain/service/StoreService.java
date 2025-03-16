package com.toanyone.store.domain.service;

import com.toanyone.store.presentation.dto.StoreCreateRequestDto;
import com.toanyone.store.presentation.dto.StoreCreateResponseDto;
import com.toanyone.store.presentation.dto.StoreFindResponseDto;
import jakarta.validation.Valid;

public interface StoreService {
    StoreCreateResponseDto createStore(StoreCreateRequestDto storeCreateRequestDto);

    StoreFindResponseDto findOne(Long storeId);

    void deleteStore(Long storeId);
}
