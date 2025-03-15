package com.toanyone.order.infrastructure;

import com.toanyone.order.domain.entity.OrderItem;
import com.toanyone.order.domain.repository.OrderItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Long>, OrderItemRepository {
}
