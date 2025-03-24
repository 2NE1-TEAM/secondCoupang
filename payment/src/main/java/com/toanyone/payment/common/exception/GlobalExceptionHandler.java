package com.toanyone.payment.common.exception;

import com.toanyone.payment.common.SingleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<SingleResponse> handleSingleCustomException(CustomException e) {
        log.error("ERROR ::: [RunTimeException] ", e);
        log.info("Error Message: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(SingleResponse.error(e.getMessage(),e.getErrorCode()));
    }


}
