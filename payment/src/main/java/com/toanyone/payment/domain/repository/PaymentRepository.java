package com.toanyone.payment.domain.repository;

import com.toanyone.payment.domain.entity.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findByOrderId(Long orderId);
}
