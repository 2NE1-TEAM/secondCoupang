package com.toanyone.hub.common.exception;

import com.toanyone.hub.domain.exception.HubException;
import com.toanyone.hub.presentation.dto.SingleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class HubExceptionHandler {
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity handleDatabaseException(DataAccessException e) {
        log.info(e.getMessage());
        return ResponseEntity.internalServerError().body(SingleResponse.error(e.getMessage(), "HUB_ERROR_1"));
    }

    @ExceptionHandler(HubException.HubDuplicateException.class)
    public ResponseEntity HubDuplicateException(HubException.HubDuplicateException e) {
        log.info(e.getMessage());
        System.out.println("test");
        System.out.println(e.getMessage());
        System.out.println("test");
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "HUB_ERROR_2"));
    }

    @ExceptionHandler(HubException.HubNotFoundException.class)
    public ResponseEntity HubNotFoundException(HubException.HubNotFoundException e) {
        log.info(e.getMessage());
        return ResponseEntity.internalServerError().body(SingleResponse.error(e.getMessage(), "HUB_ERROR_3"));
    }

    @ExceptionHandler(HubException.HubAlreadyDeletedException.class)
    public ResponseEntity HubAlreadyDeletedException(HubException.HubAlreadyDeletedException e) {
        log.info(e.getMessage());
        return ResponseEntity.internalServerError().body(SingleResponse.error(e.getMessage(), "HUB_ERROR_4"));
    }
}
