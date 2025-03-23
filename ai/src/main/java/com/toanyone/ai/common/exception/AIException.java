package com.toanyone.ai.common.exception;


import org.springframework.http.HttpStatus;

public class AIException extends CustomException {

    public AIException(String errorCode, String message, HttpStatus status) {
        super(errorCode, message, status);
    }

    public static class UnAuthorized extends AIException {
        public UnAuthorized() {
            super("AI_ERROR_1", "권한을 가지고 있지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }
}
