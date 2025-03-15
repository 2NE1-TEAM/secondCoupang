package com.toanyone.order.domain.entity;

import com.toanyone.order.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Getter
@Table(name = "p_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long supplyStoreId;

    @Column(nullable = false)
    private Long receiveStoreId;

    @Column(nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(Long userId, Long supplyStoreId, Long receiveStoreId) {
        Order order = new Order();
        order.userId = userId;
        order.supplyStoreId = supplyStoreId;
        order.receiveStoreId = receiveStoreId;
        return order;
    }

    public void addOrderItem(OrderItem item) {
        item.assignOrder(this);
        items.add(item);
    }

    public void calculateTotalPrice() {
        this.totalPrice = items.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }


    private enum OrderStatus {

        PREPARING,
        DELIVERING,
        DELIVERY_COMPLETED,
        CANCELED;

    }

}
