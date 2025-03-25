package com.toanyone.delivery.common.exception;


import com.toanyone.delivery.common.utils.SingleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> ex(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getFieldErrors();

        List<DataFormatError.ErrorField> errorFields = new ArrayList<>();
        fieldErrors.forEach(errorField -> {
            errorFields.add(new DataFormatError.ErrorField(errorField.getRejectedValue(), errorField.getField(), errorField.getDefaultMessage()));
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(DataFormatError.of("올바른 입력값을 입력해주세요.", errorFields));

    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> dataIntegrityViolationExceptionHandle(final DataIntegrityViolationException ex) {
        log.error(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        SingleResponse.error("잘못된 요청입니다.", "RQ-001"));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> customExceptionHandle(CustomException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(SingleResponse.error(ex.getMessage(), ex.getCode()));
    }

    @AllArgsConstructor
    @Getter
    public static class ErrorMessage {
        String message;
        String errorCode;

        public static ErrorMessage of(String message, String errorCode) {
            return new ErrorMessage(message, errorCode);
        }
    }

    @AllArgsConstructor
    @Getter
    public static class DataFormatError {
        private String message;
        private List<ErrorField> errorFields;

        public static DataFormatError of(String message, List<ErrorField> errorFields) {
            return new DataFormatError(message, errorFields);
        }

        @AllArgsConstructor
        @Getter
        public static class ErrorField {
            private Object value;
            private String field;
            private String message;
        }
    }
}
