package com.toanyone.ai.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
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
