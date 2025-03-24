package com.toanyone.order.infrastructure.repository;

import com.toanyone.order.domain.model.OrderItem;
import com.toanyone.order.domain.repository.OrderItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemRepository {

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.status = :status WHERE oi.order.id = :orderId")
    void bulkUpdateOrderItemsStatus(@Param("orderId") Long orderId, @Param("status") OrderItem.OrderItemStatus status);

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.status = :status, oi.deletedBy = :userId, oi.deletedAt = :timestamp WHERE oi.order.id = :orderId")
    void bulkDeleteOrderItems(@Param("orderId") Long orderId, @Param("status") OrderItem.OrderItemStatus status, @Param("userId") Long userId, @Param("timestamp") LocalDateTime timestamp);


}
