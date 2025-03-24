package com.toanyone.store.common.exception;

import com.toanyone.store.common.filter.UserContext;
import com.toanyone.store.presentation.dto.SingleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.toanyone.store.domain.exception.StoreException.*;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class StoreExceptionHandler {

    private final UserContext userContext;

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity handleDatabaseException(DataAccessException e) {
        log.info(e.getMessage());
        return ResponseEntity.internalServerError().body(SingleResponse.error("데이터베이스 오류가 발생했습니다.", "STORE_ERROR_1"));
    }

    @ExceptionHandler(HubNotFoundException.class)
    public ResponseEntity HubNotFoundException(HubNotFoundException e) {
        log.info("userId: {}", userContext.getUser().getUserId(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SingleResponse.error(e.getMessage(), "STORE_ERROR_2"));
    }

    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity StoreNotFoundException(StoreNotFoundException e) {
        log.info("userId: {}", userContext.getUser().getUserId(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SingleResponse.error(e.getMessage(), "STORE_ERROR_3"));
    }

    @ExceptionHandler(StoreAlreadyDeletedException.class)
    public ResponseEntity StoreAlreadyDeletedException(StoreAlreadyDeletedException e) {
        log.info("userId: {}", userContext.getUser().getUserId(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SingleResponse.error(e.getMessage(), "STORE_ERROR_4"));
    }

    @ExceptionHandler(StoreDuplicateException.class)
    public ResponseEntity StoreNameExistException(StoreDuplicateException e) {
        log.info("userId: {}", userContext.getUser().getUserId(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SingleResponse.error(e.getMessage(), "STORE_ERROR_6"));
    }

    @ExceptionHandler(HubServerErrorException.class)
    public ResponseEntity HubServerErrorException(HubServerErrorException e) {
        log.info("userId: {}", userContext.getUser().getUserId(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(SingleResponse.error(e.getMessage(), "STORE_ERROR_7"));
    }

    @ExceptionHandler(StoreDeniedException.class)
    public ResponseEntity StoreDeniedException(StoreDeniedException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "STORE_ERROR_8"));
    }
}
