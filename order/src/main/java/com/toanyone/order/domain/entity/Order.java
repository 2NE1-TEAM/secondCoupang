package com.toanyone.order.domain.entity;

import com.toanyone.order.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


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

    @OneToMany
    @JoinColumn(name = "order_id")
    private List<OrderItem> items;

    private enum OrderStatus {

        PREPARING,
        DELIVERING,
        DELIVERY_COMPLETED,
        CANCELED;

    }

}
