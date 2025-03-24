package com.toanyone.order.domain.entity;

import com.toanyone.order.common.BaseEntity;
import com.toanyone.order.common.exception.OrderException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Entity
@Getter
@Table(name = "p_order")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String ordererName;

    @Column(nullable = false)
    private Long supplyStoreId;

    @Column(nullable = false)
    private Long receiveStoreId;

    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false)
    private String request;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<OrderItem> items = new ArrayList<>();

    public static Order create(Long userId, String ordererName, String request, Long supplyStoreId, Long receiveStoreId) {
        Order order = new Order();
        order.userId = userId;
        order.ordererName = ordererName;
        order.request = request;
        order.supplyStoreId = supplyStoreId;
        order.receiveStoreId = receiveStoreId;
        order.status = OrderStatus.PAYMENT_WAITING;
        return order;
    }


    public void addOrderItem(OrderItem item) {
        item.assignOrder(this);
        items.add(item);
    }


    public void calculateTotalPrice() {
        this.totalPrice = items.stream().mapToInt(OrderItem::getTotalPrice).sum();
    }

    public void completedPayment() {
        if (this.status != OrderStatus.PAYMENT_WAITING) {
            log.info("PAYMENT_WAITING -> PREPARING");
            throw new OrderException.OrderStatusIllegalException();
        }
        this.status = OrderStatus.PREPARING;
    }

    public void paymentCancelRequested() {
        if (this.status != OrderStatus.PREPARING) {
            log.info("PREPARING -> PAYMENT_CANCEL_REQUESTED");
            throw new OrderException.OrderStatusIllegalException();
        }
        this.status = OrderStatus.PAYMENT_CANCEL_REQUESTED;
    }

    public void startDelivery() {
        if (this.status != OrderStatus.PREPARING) {
            log.info("PREPARING -> DELIVERING");
            throw new OrderException.OrderStatusIllegalException();
        }
        this.status = OrderStatus.DELIVERING;
    }

    public void completedDelivery() {
        if (this.status != OrderStatus.DELIVERING) {
            log.info("DELIVERING -> DELIVERY_COMPLETED");
            throw new OrderException.OrderStatusIllegalException();
        }
        this.status = OrderStatus.DELIVERY_COMPLETED;
    }


    public void cancel() {
        if (this.status == OrderStatus.PREPARING || this.status == OrderStatus.PAYMENT_WAITING || this.status == OrderStatus.PAYMENT_CANCEL_REQUESTED) {
            this.status = OrderStatus.CANCELED;
        } else {
            throw new OrderException.OrderStatusIllegalException();
        }
    }

    @Override
    public void delete(Long userId) {
        if (this.deletedAt != null) throw new OrderException.OrderAlreadyDeletedException();
        super.delete(userId);
        this.status = OrderStatus.CANCELED;
    }
    

    @Getter
    @AllArgsConstructor
    public enum OrderStatus {

        PAYMENT_WAITING("결제 진행 중"),
        PREPARING("배송 준비 중"),
        DELIVERING("배송 중"),
        DELIVERY_COMPLETED("배송 완료"),
        PAYMENT_CANCEL_REQUESTED("결제 취소 진행 중"),
        CANCELED("주문 취소");

        private final String description;

    }

}
