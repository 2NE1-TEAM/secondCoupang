package com.toanyone.delivery.application.exception;

import com.toanyone.delivery.common.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DeliveryManagerException extends CustomException {

    public DeliveryManagerException(String message, HttpStatus status) {
        super(message, status);
    }

    public static class AlreadyExistsUserException extends DeliveryManagerException {
        public AlreadyExistsUserException() {
            super("해당 회원은 이미 배송담당자로 등록되어있습니다.", HttpStatus.CONFLICT);
        }
    }

    public static class InvalidDeliveryManagerTypeException extends DeliveryManagerException {
        public InvalidDeliveryManagerTypeException() {
            super("존재하지 않는 담당자 타입입니다", HttpStatus.BAD_REQUEST);
        }
    }
}

