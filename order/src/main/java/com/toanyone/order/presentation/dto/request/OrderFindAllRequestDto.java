package com.toanyone.order.presentation.dto.request;

import com.toanyone.order.common.dto.SortType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.toanyone.order.common.dto.CursorConstants.DEFAULT_SIZE;
import static com.toanyone.order.common.dto.CursorConstants.DEFAULT_SORT_TYPE;

@Getter
public class OrderFindAllRequestDto {

    private int size;
    private Long nextCursorOrderId;
    private LocalDateTime timestamp;
    private SortType sortType;

    @Builder
    public OrderFindAllRequestDto(Integer size, Long nextCursorOrderId, LocalDateTime timestamp, SortType sortType) {
        this.size = (size != null) ? size : DEFAULT_SIZE;
        this.nextCursorOrderId = nextCursorOrderId;
        this.timestamp = timestamp;
        this.sortType = (sortType != null) ? sortType : DEFAULT_SORT_TYPE;
    }

}