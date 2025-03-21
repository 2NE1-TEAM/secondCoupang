package com.toanyone.gateway.common.exception.exception;



import com.toanyone.gateway.common.exception.response.SingleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<SingleResponse> handleSingleCustomException(CustomException e) {
        log.error("ERROR ::: [RunTimeException] ", e);
        log.info("Error Message: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(SingleResponse.error(e.getMessage(),e.getErrorCode()));
    }


}
