package com.toanyone.delivery.infrastructure;

import com.toanyone.delivery.domain.Delivery;
import com.toanyone.delivery.domain.repository.DeliveryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepositoryImpl extends JpaRepository<Delivery, Long>, DeliveryRepository {

}
