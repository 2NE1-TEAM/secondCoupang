package com.toanyone.user.user.common.exception;


import org.springframework.http.HttpStatus;

public class UserException extends CustomException {

    public UserException(String errorCode, String message, HttpStatus status) {
        super(errorCode, message, status);
    }

    public static class AlreadyExistedId extends UserException {
        public AlreadyExistedId() {
            super("USER_ERROR_1", "이미 존재하는 Slack ID 입니다.", HttpStatus.BAD_REQUEST);
        }
    }

    public static class NoExistId extends UserException {
        public NoExistId() {
            super("USER_ERROR_2", "아이디가 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    public static class NotCorrectPassword extends UserException {
        public NotCorrectPassword() {
            super("USER_ERROR_3", "옳바르지 않은 비밀번호 입니다.", HttpStatus.BAD_REQUEST);
        }
    }

}
