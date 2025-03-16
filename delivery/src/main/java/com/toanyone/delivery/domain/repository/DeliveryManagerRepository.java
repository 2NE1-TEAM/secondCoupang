package com.toanyone.delivery.domain.repository;

import com.toanyone.delivery.domain.DeliveryManager;

import java.util.Optional;

public interface DeliveryManagerRepository {
    DeliveryManager save(DeliveryManager deliveryManager);

    Optional<DeliveryManager> findById(Long id);

    Boolean existsByUserId(Long id);
    
}
