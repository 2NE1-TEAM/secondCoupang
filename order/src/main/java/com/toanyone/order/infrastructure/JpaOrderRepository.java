package com.toanyone.order.infrastructure;

import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.repository.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long>, OrderRepository {


}
