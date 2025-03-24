package com.toanyone.store.infrastructure.repository;

import com.toanyone.store.domain.model.Store;
import com.toanyone.store.presentation.dto.CursorPage;
import com.toanyone.store.presentation.dto.StoreSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreRepositoryCustom {
    CursorPage<Store> search(StoreSearchRequest storeSearchRequest, String sortBy, String direction, int size);
}
