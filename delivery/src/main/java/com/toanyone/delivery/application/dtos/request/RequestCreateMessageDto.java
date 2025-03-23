package com.toanyone.delivery.application.dtos.request;

import lombok.Builder;

@Builder
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
