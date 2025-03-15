package com.toanyone.order.domain.entity;

import com.toanyone.order.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Table(name = "p_order_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    public static OrderItem create(Long itemId, String itemName, int quantity, int price) {
        OrderItem orderItem = new OrderItem();
        orderItem.itemId = itemId;
        orderItem.itemName = itemName;
        orderItem.quantity = quantity;
        orderItem.price = price;
        return orderItem;
    }

    public int getTotalPrice() {
        return this.price * this.quantity;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }
}
