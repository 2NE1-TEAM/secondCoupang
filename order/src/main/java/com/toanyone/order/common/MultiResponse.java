package com.toanyone.order.common;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class MultiResponse<T> {
    private final List<T> data;
    private final int count;
    private final int totalPages;
    private final long totalElements;
    private final int currentPage;
    private final int pageSize;
    private final String errorMessage;
    private final String errorCode;

    // 성공 응답
    public MultiResponse(List<T> data) {
        this.data = data;
        this.count = data.size();
        this.totalPages = 1;
        this.totalElements = data.size();
        this.currentPage = 0;
        this.pageSize = data.size();
        this.errorMessage = null;
        this.errorCode = null;
    }

    // 성공 응답 (페이징)
    public MultiResponse(Page<T> page) {
        this.data = page.getContent();
        this.count = page.getNumberOfElements();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.errorMessage = null;
        this.errorCode = null;
    }

    // 에러 응답
    public MultiResponse(String errorMessage, String errorCode) {
        this.data = null;
        this.count = 0;
        this.totalPages = 0;
        this.totalElements = 0;
        this.currentPage = 0;
        this.pageSize = 0;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static <T> MultiResponse<T> success(List<T> data) {
        return new MultiResponse<>(data);
    }

    public static <T> MultiResponse<T> success(Page<T> page) {
        return new MultiResponse<>(page);
    }

    public static <T> MultiResponse<T> error(String errorMessage, String errorCode) {
        return new MultiResponse<>(errorMessage, errorCode);
    }
}