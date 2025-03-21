package com.toanyone.order.domain.repository;

import com.toanyone.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface OrderItemRepository {

    void bulkUpdateOrderItemsStatus(Long orderId, OrderItem.OrderItemStatus status);

    void bulkDeleteOrderItems(Long orderId, OrderItem.OrderItemStatus status, Long userId, LocalDateTime timestamp);

}
