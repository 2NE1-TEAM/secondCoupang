package com.toanyone.order.domain.repository;

import com.toanyone.order.domain.entity.Order;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);
}
