package com.toanyone.order.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {

    private String errorCode;
    private String message;
    private HttpStatus status;

}