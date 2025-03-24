package com.toanyone.order.application.dto.request;

import com.toanyone.order.common.dto.SortType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderFindAllCondition {
    private int size;
    private Long cursorId;
    private LocalDateTime timestamp;
    private SortType sortType;
}