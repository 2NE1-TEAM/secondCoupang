package com.toanyone.delivery.application.exception;

import com.toanyone.delivery.common.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DeliveryManagerException extends CustomException {

    public DeliveryManagerException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);
    }

    public static class AlreadyExistsUserException extends DeliveryManagerException {
        public AlreadyExistsUserException() {
            super("해당 회원은 이미 배송담당자로 등록되어있습니다.", HttpStatus.CONFLICT, "DELIVERY-MANAGER-CONFLICT");
        }
    }

    public static class NotFoundManagerException extends DeliveryManagerException {
        public NotFoundManagerException() {
            super("존재하지 않는 배송담당자입니다.", HttpStatus.NOT_FOUND, "DELIVERY-MANAGER-NOT_FOUND");
        }
    }

    public static class InvalidDeliveryManagerTypeException extends DeliveryManagerException {
        public InvalidDeliveryManagerTypeException() {
            super("존재하지 않는 담당자 타입입니다", HttpStatus.BAD_REQUEST, "DELIVERY-MANAGER-INVALID-TYPE");
        }
    }

    public static class UnauthorizedDeliveryManagerEditException extends DeliveryManagerException {
        public UnauthorizedDeliveryManagerEditException() {
            super("해당 유저는 담당 매니저 정보 수정 권한이 없습니다.", HttpStatus.FORBIDDEN, "DELIVERY-MANAGER-UNAUTHORIZED-EDIT");
        }
    }

    public static class UnauthorizedDeliveryManagerDeleteException extends DeliveryManagerException {
        public UnauthorizedDeliveryManagerDeleteException() {
            super("해당 유저는 담당 매니저 정보 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN, "DELIVERY-MANAGER-UNAUTHORIZED-DELETE");
        }
    }

    public static class InvalidHubException extends DeliveryManagerException {
        public InvalidHubException() {
            super("등록하려는 배송담당자의 소속허브정보가 존재하지 않습니다", HttpStatus.BAD_REQUEST, "DELIVERY-MANAGER-INVALID-HUB");
        }
    }
}

