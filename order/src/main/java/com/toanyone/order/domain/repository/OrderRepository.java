package com.toanyone.order.domain.repository;

import com.toanyone.order.domain.entity.Order;

public interface OrderRepository {

    Order save(Order order);
}
