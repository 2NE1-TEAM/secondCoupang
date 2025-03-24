package com.toanyone.store.domain.repository;

import com.toanyone.store.domain.model.Location;
import com.toanyone.store.domain.model.Store;
import com.toanyone.store.infrastructure.repository.StoreRepositoryCustom;
import com.toanyone.store.presentation.dto.StoreSearchRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreRepositoryCustom {
    boolean existsByStoreName(String storeName);

    boolean existsByLocation(Location location);

    boolean existsByTelephone(String telephone);

    boolean existsByStoreNameAndIdNot(String storeName, Long id);

    boolean existsByLocationAndIdNot(Location location, Long id);

    boolean existsByTelephoneAndIdNot(String telephone, Long id);
}
