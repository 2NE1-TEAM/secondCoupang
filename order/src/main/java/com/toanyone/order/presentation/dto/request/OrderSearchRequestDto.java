package com.toanyone.order.presentation.dto.request;

import com.toanyone.order.presentation.dto.SortType;
import lombok.*;

import java.time.LocalDateTime;

import static com.toanyone.order.common.CursorConstants.DEFAULT_SIZE;
import static com.toanyone.order.common.CursorConstants.DEFAULT_SORT_TYPE;

@Getter
public class OrderSearchRequestDto {

    private String keyword; //주문상품 이름
    private Long userId; //자신 주문만 보는 경우 (for 허브, 배송, 업체)
    private Long storeId; // 공급 업체 주문 보는 경우
    private Long hubId; //Todo: 허브 주문만 보는 경우 (for hub 담당자)
    private int size;
    private Long cursorId;
    private LocalDateTime timestamp;
    private SortType sortType;

    @Builder
    public OrderSearchRequestDto(String keyword, Long userId, Long hubId, Long storeId, Integer size, Long cursorId, LocalDateTime timestamp, SortType sortType) {
        this.keyword = keyword;
        this.userId = userId;
        this.hubId = hubId;
        this.storeId = storeId;
        this.size = (size != null) ? size : DEFAULT_SIZE;
        this.cursorId = cursorId;
        this.timestamp = timestamp;
        this.sortType = (sortType != null) ? sortType : DEFAULT_SORT_TYPE;
    }

}