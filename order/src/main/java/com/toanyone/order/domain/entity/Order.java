package com.toanyone.order.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toanyone.order.common.BaseEntity;
import com.toanyone.order.common.exception.OrderException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
    @Column(name = "order_status", nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(Long userId, Long supplyStoreId, Long receiveStoreId) {
        Order order = new Order();
        order.userId = userId;
        order.supplyStoreId = supplyStoreId;
        order.receiveStoreId = receiveStoreId;
        order.status = OrderStatus.PREPARING;
        return order;
    }

    public void addOrderItem(OrderItem item) {
        item.assignOrder(this);
        items.add(item);
    }


    public void calculateTotalPrice() {
        this.totalPrice = items.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }

    public void startDelivery() {
        if (this.status != OrderStatus.PREPARING) {
            throw new OrderException.OrderStatusIllegalException();
        }
        this.status = OrderStatus.DELIVERING;
    }

    public void completedDelivery() {
        if (this.status != OrderStatus.DELIVERING) {
            throw new OrderException.OrderStatusIllegalException();
        }
        this.status = OrderStatus.DELIVERY_COMPLETED;
    }

    public void cancel() {
        if (this.status != OrderStatus.PREPARING) {
            throw new OrderException.OrderStatusIllegalException();
        }
        this.status = OrderStatus.CANCELED;
        this.items.stream().forEach(OrderItem::cancel);
    }

    @Override
    public void delete(Long userId) {
        if (this.deletedAt != null) throw new OrderException.OrderAlreadyDeletedException();
        super.delete(userId);
        this.status = OrderStatus.CANCELED;
        for (OrderItem orderItem : this.items) {
            orderItem.delete(userId);
        }
    }
    

    @Getter
    @AllArgsConstructor
    public enum OrderStatus {

        PREPARING("배송 준비 중"),
        DELIVERING("배송 중"),
        DELIVERY_COMPLETED("배송 완료"),
        CANCELED("주문 취소");

        private final String description;

    }

}
