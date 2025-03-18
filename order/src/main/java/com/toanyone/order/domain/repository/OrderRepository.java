package com.toanyone.order.domain.repository;

import com.toanyone.order.common.CursorPage;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.infrastructure.OrderQueryDslRepository;
import com.toanyone.order.presentation.dto.request.OrderSearchRequestDto;

import java.util.Optional;

public interface OrderRepository extends OrderQueryDslRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    Optional<Order> findByIdWithItems(Long id);

    CursorPage<Order> search(OrderSearchRequestDto requestDto, int size);
}
