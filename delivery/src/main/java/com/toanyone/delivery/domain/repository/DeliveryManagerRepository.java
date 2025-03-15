package com.toanyone.delivery.domain.repository;

import com.toanyone.delivery.domain.DeliveryManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryManagerRepository {
    DeliveryManager save(DeliveryManager deliveryManager);

    Optional<DeliveryManager> findById(Long id);

    Boolean existsByUserId(Long id);
}
