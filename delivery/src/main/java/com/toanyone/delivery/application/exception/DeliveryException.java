package com.toanyone.delivery.application.exception;

import com.toanyone.delivery.common.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DeliveryException extends CustomException {
    public DeliveryException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);
    }

    public static class DeliveryNotFoundException extends DeliveryException {
        public DeliveryNotFoundException() {
            super("존재하지 않는 배송정보입니다.", HttpStatus.NOT_FOUND, "DELIVERY-NOT-FOUND");
        }
    }

    public static class InvalidDeliveryTypeException extends DeliveryException {
        public InvalidDeliveryTypeException() {
            super("존재하지 않는 배송상태입니다", HttpStatus.BAD_REQUEST, "INVALID_DELIVERY-TYPE");
        }
    }

    public static class UnauthorizedDeliveryUpdateException extends DeliveryException {
        public UnauthorizedDeliveryUpdateException() {
            super("해당 유저는 배송정보 수정 권한이 없습니다.", HttpStatus.FORBIDDEN, "UNAUTHORIZED-DELIVERY-UPDATE");
        }
    }

    public static class UnauthorizedDeliveryDeleteException extends DeliveryException {
        public UnauthorizedDeliveryDeleteException() {
            super("해당 유저는 배송정보 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN, "UNAUTHORIZED-DELIVERY-DELETE");
        }
    }
}
