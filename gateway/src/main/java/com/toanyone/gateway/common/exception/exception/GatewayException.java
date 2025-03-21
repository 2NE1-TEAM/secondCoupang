package com.toanyone.gateway.common.exception.exception;


import org.springframework.http.HttpStatus;

public class GatewayException extends CustomException {

    public GatewayException(String errorCode, String message, HttpStatus status) {
        super(errorCode, message, status);
    }

    public static class InvalidToken extends GatewayException {
        public InvalidToken() {
            super("GATEWAY_ERROR_1", "토큰이 유효하지 않거나 토큰이 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }
}
