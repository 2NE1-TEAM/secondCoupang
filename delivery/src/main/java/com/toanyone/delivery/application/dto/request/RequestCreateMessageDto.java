package com.toanyone.delivery.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class RequestCreateMessageDto {
    private Long orderId;
    private String orderNickName;
    private String orderSlackId;
    private String itemInfo;
    private String request;
    private String shippingAddress;
    private String stopOver;
    private String destination;
    private String deliveryPerson;
    private String deliveryPersonSlackId;
}
