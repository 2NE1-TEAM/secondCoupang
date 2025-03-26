package com.toanyone.order.presentation.dto.request;

import com.toanyone.order.common.dto.SortType;
import lombok.*;

import java.time.LocalDateTime;

import static com.toanyone.order.common.dto.CursorConstants.DEFAULT_SIZE;
import static com.toanyone.order.common.dto.CursorConstants.DEFAULT_SORT_TYPE;

@Getter
public class OrderSearchRequestDto {

    private String keyword;
    private Long userId;
    private Long storeId;
    private Long hubId;
    private int size;
    private Long nextCursorOrderId;
    private LocalDateTime timestamp;
    private SortType sortType;

    @Builder
    public OrderSearchRequestDto(String keyword, Long userId, Long hubId, Long storeId, Integer size, Long nextCursorOrderId, LocalDateTime timestamp, SortType sortType) {
        this.keyword = keyword;
        this.userId = userId;
        this.hubId = hubId;
        this.storeId = storeId;
        this.size = (size != null) ? size : DEFAULT_SIZE;
        this.nextCursorOrderId = nextCursorOrderId;
        this.timestamp = timestamp;
        this.sortType = (sortType != null) ? sortType : DEFAULT_SORT_TYPE;
    }

}