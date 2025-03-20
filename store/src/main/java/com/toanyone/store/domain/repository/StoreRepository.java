package com.toanyone.store.domain.repository;

import com.toanyone.store.domain.model.Store;
import com.toanyone.store.infrastructure.repository.StoreRepositoryCustom;
import com.toanyone.store.presentation.dto.StoreSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
    Optional<Store> findByStoreName(String name);
}
