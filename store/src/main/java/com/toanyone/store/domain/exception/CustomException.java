package com.toanyone.store.domain.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomException extends RuntimeException {
    private String message;
}
