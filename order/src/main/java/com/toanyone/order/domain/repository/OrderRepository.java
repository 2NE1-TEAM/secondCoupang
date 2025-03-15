package com.toanyone.order.domain.repository;

import com.toanyone.order.domain.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository {

    Order save(Order order);
}
