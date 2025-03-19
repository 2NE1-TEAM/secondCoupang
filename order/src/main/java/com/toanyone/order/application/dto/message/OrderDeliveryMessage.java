package com.toanyone.order.application.dto.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderDeliveryMessage {
//    private Long userId; userId, Role, slackId는 헤더에 넣어서 보내주기
    private Long supplyStoreId;
    private Long receiveStoreId;
    private String recipient;
}
