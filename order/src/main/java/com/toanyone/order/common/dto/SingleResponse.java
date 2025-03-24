package com.toanyone.order.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SingleResponse<T> implements Serializable {
    private T data;
    private String errorMessage;
    private String errorCode;

    //성공 응답
    public SingleResponse(T data) {
        this.data = data;
        this.errorMessage = null;
        this.errorCode = null;
    }

    //에러응답
    public SingleResponse(String errorMessage, String errorCode) {
        this.data = null;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static <T> SingleResponse<T> success(T data) {
        return new SingleResponse<>(data);
    }

    public static <T> SingleResponse<T> error(String errorMessage, String errorCode) {
        return new SingleResponse<>(errorMessage, errorCode);
    }
}