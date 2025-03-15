package com.toanyone.delivery.domain.repository;

import com.toanyone.delivery.domain.Delivery;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository {
    Delivery save(Delivery delivery);
}
