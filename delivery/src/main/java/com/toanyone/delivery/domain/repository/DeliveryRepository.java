package com.toanyone.delivery.domain.repository;

import com.toanyone.delivery.domain.Delivery;

import java.util.Optional;

public interface DeliveryRepository {
    Delivery save(Delivery delivery);

    Optional<Delivery> findById(Long id);

}
