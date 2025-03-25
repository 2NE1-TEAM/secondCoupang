package com.toanyone.delivery.common.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
public class MultiResponse<T> {
    private final List<T> data;
    private final CursorInfo nextCursor; // 다음 페이지 요청할 떄 줘야 할 값. 이 값은 정렬 기준이 뭐가 있냐에 따라 다름.
    private final boolean hasNext; // 다음 페이지 존재 여부
    private final String errorMessage;
    private final String errorCode;

    // 성공 응답 (커서 페이징 기반)
    public MultiResponse(CursorPage<T> page) {
        this.data = page.getContent();
        this.nextCursor = page.getNextCursor();
        this.hasNext = page.isHasNext();
        this.errorMessage = null;
        this.errorCode = null;
    }

    // 에러 응답
    public MultiResponse(String errorMessage, String errorCode) {
        this.data = null;
        this.nextCursor = null;
        this.hasNext = false;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static <T> MultiResponse<T> success(CursorPage<T> page) {
        return new MultiResponse<>(page);
    }

    public static <T> MultiResponse<T> error(String errorMessage, String errorCode) {
        return new MultiResponse<>(errorMessage, errorCode);
    }

    @AllArgsConstructor
    @Getter
    public static class CursorInfo {
        private Long nextCursorElementId;

    }

    @Getter
    @AllArgsConstructor
    public static class CursorPage<T> {
        private List<T> content; // 조회된 데이터 리스트
        private CursorInfo nextCursor; // 다음 페이지 요청할 때 우리에게 다시 보내야 될 값
        private boolean hasNext; // 다음 페이지 존재 여부
    }
}