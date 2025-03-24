package com.toanyone.order.domain.repository;

import com.toanyone.order.domain.model.OrderItem;

import java.time.LocalDateTime;

public interface OrderItemRepository {

    void bulkUpdateOrderItemsStatus(Long orderId, OrderItem.OrderItemStatus status);

    void bulkDeleteOrderItems(Long orderId, OrderItem.OrderItemStatus status, Long userId, LocalDateTime timestamp);

}
