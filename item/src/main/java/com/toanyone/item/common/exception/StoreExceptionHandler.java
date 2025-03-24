package com.toanyone.item.common.exception;

import com.toanyone.item.common.filter.UserContext;
import com.toanyone.item.domain.exception.ItemException;
import com.toanyone.item.presentation.dto.SingleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class StoreExceptionHandler {

    private final UserContext userContext;

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity handleDatabaseException(DataAccessException e) {
        log.info(e.getMessage());
        return ResponseEntity.internalServerError().body(SingleResponse.error("데이터베이스 오류가 발생했습니다.", "ITEM_ERROR_1"));
    }

    @ExceptionHandler(ItemException.ItemAlreadyDeletedException.class)
    public ResponseEntity ItemAlreadyDeletedException(ItemException.ItemAlreadyDeletedException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "ITEM_ERROR_2"));
    }

    @ExceptionHandler(ItemException.ItemNotFoundException.class)
    public ResponseEntity ItemNotFoundException(ItemException.ItemNotFoundException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "ITEM_ERROR_3"));
    }

    @ExceptionHandler(ItemException.DeniedException.class)
    public ResponseEntity DeniedException(ItemException.DeniedException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "ITEM_ERROR_4"));
    }

    @ExceptionHandler(ItemException.StoreNotFoundException.class)
    public ResponseEntity StoreNotFoundException(ItemException.StoreNotFoundException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "ITEM_ERROR_5"));
    }

    @ExceptionHandler(ItemException.StoreServerErrorException.class)
    public ResponseEntity StoreServerErrorException(ItemException.StoreServerErrorException e) {
        log.info(e.getMessage());
        return ResponseEntity.internalServerError().body(SingleResponse.error(e.getMessage(), "ITEM_ERROR_6"));
    }

    @ExceptionHandler(ItemException.StockReduceException.class)
    public ResponseEntity StockReduceException(ItemException.StockReduceException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "ITEM_ERROR_7"));
    }

    @ExceptionHandler(ItemException.StockAddException.class)
    public ResponseEntity StockAddException(ItemException.StockAddException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "ITEM_ERROR_8"));
    }

    @ExceptionHandler(ItemException.StockZeroException.class)
    public ResponseEntity StockAddException(ItemException.StockZeroException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "ITEM_ERROR_9"));
    }

    @ExceptionHandler(ItemException.StockBadRequestException.class)
    public ResponseEntity StockBadRequestException(ItemException.StockBadRequestException e) {
        log.info(e.getMessage());
        return ResponseEntity.badRequest().body(SingleResponse.error(e.getMessage(), "ITEM_ERROR_10"));
    }
}
