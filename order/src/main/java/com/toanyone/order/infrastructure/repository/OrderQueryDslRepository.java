package com.toanyone.order.infrastructure.repository;

import com.toanyone.order.application.dto.request.OrderFindAllCondition;
import com.toanyone.order.application.dto.request.OrderSearchCondition;
import com.toanyone.order.common.dto.CursorPage;
import com.toanyone.order.domain.model.Order;

public interface OrderQueryDslRepository {

    CursorPage<Order> search(OrderSearchCondition requestDto);

    CursorPage<Order> findAll(Long userId, OrderFindAllCondition requestDto);

}
