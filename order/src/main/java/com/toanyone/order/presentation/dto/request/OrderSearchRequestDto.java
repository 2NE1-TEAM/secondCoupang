package com.toanyone.order.presentation.dto.request;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class OrderSearchRequestDto {

    private String keyword; //주문상품 이름
    private Long userId; //자신 주문만 보는 경우 (for 허브, 배송, 업체)
    private Long storeId; // 공급 업체 주문 보는 경우
    private Long hubId; //Todo: 허브 주문만 보는 경우 (for hub 담당자)
    private Long cursorId;
    private LocalDateTime timestamp;
    private SortType sortType = SortType.CREATED_AT_DESC;

    public enum SortType {
        CREATED_AT_DESC,
        UPDATED_AT_DESC;

    }

}