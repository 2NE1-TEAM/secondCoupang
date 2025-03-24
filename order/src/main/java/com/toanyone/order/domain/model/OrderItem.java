package com.toanyone.order.domain.model;

import com.toanyone.order.common.model.BaseEntity;
import com.toanyone.order.common.exception.OrderException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    private OrderItemStatus status;

    public static OrderItem create(Long itemId, String itemName, int quantity, int price) {
        OrderItem orderItem = new OrderItem();
        orderItem.itemId = itemId;
        orderItem.itemName = itemName;
        orderItem.quantity = quantity;
        orderItem.price = price;
        orderItem.status = OrderItemStatus.PREPARING;
        return orderItem;
    }

    public int getTotalPrice() {
        return this.price * this.quantity;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }

    public void cancel() {
        if (this.status != OrderItemStatus.PREPARING){
            throw new OrderException.OrderItemCancelFailedException();
        }
        this.status = OrderItemStatus.CANCELED;
    }

    @Getter
    @AllArgsConstructor
    public enum OrderItemStatus {
        PREPARING("상품 준비 중"),

        CANCELED("상픔 취소");


        private final String description;
    }

}
