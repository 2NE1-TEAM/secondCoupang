package com.toanyone.order.infrastructure;

import com.toanyone.order.common.CursorPage;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.repository.OrderRepository;
import com.toanyone.order.presentation.dto.request.OrderFindAllRequestDto;
import com.toanyone.order.presentation.dto.request.OrderSearchRequestDto;

public interface OrderQueryDslRepository {

    CursorPage<Order> search(OrderSearchRequestDto requestDto);

    CursorPage<Order> findAll(Long userId, OrderFindAllRequestDto requestDto);

}
