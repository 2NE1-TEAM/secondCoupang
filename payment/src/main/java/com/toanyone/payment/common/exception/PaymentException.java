package com.toanyone.payment.common.exception;


import org.springframework.http.HttpStatus;

public class PaymentException extends CustomException {
    public PaymentException(String errorCode, String message, HttpStatus status) {
        super(errorCode, message, status);
    }

    public static class PaymentBadRequestException extends PaymentException {
        public PaymentBadRequestException() {
            super("PAYMENT_ERROR_1", "잘못된 결제 요청입니다.", HttpStatus.BAD_REQUEST);
        }
    }

    public static class PaymentNotFoundException extends PaymentException {
        public PaymentNotFoundException() { super("PAYMENT_ERROR_2", "결제가 존재하지 않습니다.", HttpStatus.NOT_FOUND); }

    }

    public static class PaymentAlreadyExistsException extends PaymentException {
        public PaymentAlreadyExistsException() { super("PAYMENT_ERROR_3", "이미 결제가 완료된 주문입니다.", HttpStatus.CONFLICT); }

    }


}

