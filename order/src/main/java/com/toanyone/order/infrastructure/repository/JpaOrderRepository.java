package com.toanyone.order.infrastructure.repository;

import com.toanyone.order.domain.model.Order;
import com.toanyone.order.domain.repository.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaOrderRepository extends JpaRepository<Order, Long> , OrderQueryDslRepository, OrderRepository {

    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(Long orderId);

}
