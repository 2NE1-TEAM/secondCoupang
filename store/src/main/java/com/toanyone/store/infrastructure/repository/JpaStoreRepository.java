package com.toanyone.store.infrastructure.repository;

import com.toanyone.store.domain.model.Store;
import com.toanyone.store.domain.repository.StoreRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaStoreRepository extends JpaRepository<Store, Long>, StoreRepository, StoreRepositoryCustom {
}
