package com.toanyone.delivery.infrastructure;


import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryManagerRepositoryImpl extends JpaRepository<DeliveryManager, Long>, DeliveryManagerRepository {
}
