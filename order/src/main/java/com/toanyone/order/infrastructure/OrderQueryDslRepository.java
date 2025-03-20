package com.toanyone.order.infrastructure;

import com.toanyone.order.application.dto.request.OrderFindAllCondition;
import com.toanyone.order.application.dto.request.OrderSearchCondition;
import com.toanyone.order.common.CursorPage;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.repository.OrderRepository;
import com.toanyone.order.presentation.dto.request.OrderFindAllRequestDto;
import com.toanyone.order.presentation.dto.request.OrderSearchRequestDto;

public interface OrderQueryDslRepository {

    CursorPage<Order> search(OrderSearchCondition requestDto);

    CursorPage<Order> findAll(Long userId, OrderFindAllCondition requestDto);

}
