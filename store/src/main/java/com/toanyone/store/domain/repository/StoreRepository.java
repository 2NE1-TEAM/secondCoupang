package com.toanyone.store.domain.repository;

import com.toanyone.store.domain.model.Store;

import java.util.Optional;

public interface StoreRepository {

    Store save(Store store);

    Optional<Store> findById(Long id);

    Optional<Store> findByStoreName(String name);
}
