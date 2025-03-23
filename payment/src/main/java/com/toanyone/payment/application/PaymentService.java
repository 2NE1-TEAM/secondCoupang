package com.toanyone.payment.application;

import com.toanyone.payment.common.exception.PaymentException;
import com.toanyone.payment.domain.entity.Payment;
import com.toanyone.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment processPaymentRequest(Long orderId, int amount) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            throw new PaymentException.PaymentAlreadyExistsException();
        });

        Payment payment = Payment.create(amount, orderId);

        return paymentRepository.save(payment);
    }


    @Transactional
    public Payment processPaymentCancelRequest(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(PaymentException.PaymentNotFoundException::new);

        payment.cancel();

        return payment;
    }

}
