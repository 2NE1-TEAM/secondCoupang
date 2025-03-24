package com.toanyone.order.domain.repository;

import com.toanyone.order.application.dto.request.OrderSearchCondition;
import com.toanyone.order.common.dto.CursorPage;
import com.toanyone.order.domain.model.Order;
import com.toanyone.order.infrastructure.repository.OrderQueryDslRepository;

import java.util.Optional;

public interface OrderRepository extends OrderQueryDslRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByIdWithItems(Long id);

    CursorPage<Order> search(OrderSearchCondition requestDto);
}
