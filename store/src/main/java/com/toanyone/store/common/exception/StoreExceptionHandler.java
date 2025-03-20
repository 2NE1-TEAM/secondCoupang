package com.toanyone.store.common.exception;

import com.toanyone.store.common.filter.UserContext;
import com.toanyone.store.domain.exception.StoreException;
import com.toanyone.store.presentation.dto.SingleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.toanyone.store.domain.exception.StoreException.*;

@RestControllerAdvice
@Slf4j
public class StoreExceptionHandler {
    @ExceptionHandler(HubNotFoundException.class)
    public ResponseEntity HubNotFoundException(HubNotFoundException e) {
        log.info("userId: {}", UserContext.getUser().getUserId(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SingleResponse.error(e.getMessage(), "STORE_ERROR_1"));
    }

    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity StoreNotFoundException(StoreNotFoundException e) {
        log.info("userId: {}", UserContext.getUser().getUserId(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SingleResponse.error(e.getMessage(), "STORE_ERROR_2"));
    }

    @ExceptionHandler(StoreAlreadyDeletedException.class)
    public ResponseEntity StoreAlreadyDeletedException(StoreAlreadyDeletedException e) {
        log.info("userId: {}", UserContext.getUser().getUserId(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SingleResponse.error(e.getMessage(), "STORE_ERROR_3"));
    }

    @ExceptionHandler(StoreNameExistException.class)
    public ResponseEntity StoreNameExistException(StoreNameExistException e) {
        log.info("userId: {}", UserContext.getUser().getUserId(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SingleResponse.error(e.getMessage(), "STORE_ERROR_4"));
    }
}
