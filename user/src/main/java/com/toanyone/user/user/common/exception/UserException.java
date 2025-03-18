package com.toanyone.user.user.common.exception;


import org.springframework.http.HttpStatus;

public class UserException extends CustomException {

    public UserException(String errorCode, String message, HttpStatus status) {
        super(errorCode, message, status);
    }

    public static class AlreadyExistedSlackId extends UserException {
        public AlreadyExistedSlackId() {
            super("USER_ERROR_1", "이미 존재하는 Slack ID 입니다.", HttpStatus.BAD_REQUEST);
        }
    }

    public static class NoExistSlackId extends UserException {
        public NoExistSlackId() {
            super("USER_ERROR_2", "Slack ID가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    public static class NotCorrectPassword extends UserException {
        public NotCorrectPassword() {
            super("USER_ERROR_3", "옳바르지 않은 비밀번호 입니다.", HttpStatus.BAD_REQUEST);
        }
    }

    public static class NoExistId extends UserException {
        public NoExistId() {
            super("USER_ERROR_4", "ID가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    public static class NotAuthorize extends UserException {
        public NotAuthorize() {
            super("USER_ERROR_5", "해당 기능을 위한 권한이 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }
}
