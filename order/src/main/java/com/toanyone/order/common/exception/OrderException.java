package com.toanyone.order.common.exception;


import org.springframework.http.HttpStatus;

public class OrderException extends CustomException {

    public OrderException(String errorCode, String message, HttpStatus status) {
        super(errorCode, message, status);
    }

    public static class OrderBadRequestException extends OrderException {
        public OrderBadRequestException() {
            super("ORDER_ERROR_1", "잘못된 주문 요청입니다.", HttpStatus.BAD_REQUEST);
        }
    }

    public static class InsufficientStockException extends OrderException {
        public InsufficientStockException() {
            super("ORDER_ERROR_2", "주문 요청한 상품의 재고가 부족합니다.", HttpStatus.BAD_REQUEST);
        }

    }


}
