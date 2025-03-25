package com.toanyone.payment.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    public static Payment create(int amount, Long orderId) {
        Payment payment = new Payment();
        payment.amount = amount;
        payment.orderId = orderId;
        payment.status = PaymentStatus.PENDING;
        return payment;
    }

    public void cancel() {
        if (this.status == PaymentStatus.PENDING) {
            this.status = PaymentStatus.CANCELED;
            return;
        }
    }


    @Getter
    @AllArgsConstructor
    public enum PaymentStatus {
        PENDING("결제 대기 중"),

        CANCELED("결제 취소");

        private final String description;
    }
}
