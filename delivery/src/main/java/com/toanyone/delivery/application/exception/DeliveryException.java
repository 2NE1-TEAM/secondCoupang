package com.toanyone.delivery.application.exception;

import com.toanyone.delivery.common.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DeliveryException extends CustomException {
    public DeliveryException(String message, HttpStatus status) {
        super(message, status);
    }

    public static class DeliveryNotFoundException extends DeliveryException {
        public DeliveryNotFoundException() {
            super("존재하지 않는 배송정보입니다.", HttpStatus.NOT_FOUND);
        }
    }

    public static class InvalidDeliveryTypeException extends DeliveryException {
        public InvalidDeliveryTypeException() {
            super("존재하지 않는 배송상태입니다", HttpStatus.BAD_REQUEST);
        }
    }
}
