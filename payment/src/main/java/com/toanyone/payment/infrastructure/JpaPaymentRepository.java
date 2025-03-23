package com.toanyone.payment.infrastructure;

import com.toanyone.payment.domain.entity.Payment;
import com.toanyone.payment.domain.repository.PaymentRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentRepository extends JpaRepository<Payment, Long>, PaymentRepository {

}