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

    public static class OrderStatusIllegalException extends OrderException {
        public OrderStatusIllegalException() { super("ORDER_ERROR_3", "적절하지 않는 주문 상태 변경입니다.", HttpStatus.CONFLICT); }
    }

    public static class OrderNotFoundException extends OrderException {
        public OrderNotFoundException() { super("ORDER_ERROR_4", "주문이 존재하지 않습니다.", HttpStatus.NOT_FOUND); }

    }

    public static class OrderAlreadyExistsException extends OrderException {
        public OrderAlreadyExistsException() { super("ORDER_ERROR_5", "주문이 이미 존재합니다.", HttpStatus.CONFLICT); }

    }

    public static class RestoreInventoryFailedException extends OrderException {
        public RestoreInventoryFailedException() { super("ORDER_ERROR_6", "재고 복구가 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR); }

    }

    public static class OrderCancelFailedException extends OrderException {
        public OrderCancelFailedException() { super("ORDER_ERROR_7", "주문 취소가 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR); }

    }

}
