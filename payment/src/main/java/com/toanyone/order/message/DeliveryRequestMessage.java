package com.toanyone.order.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequestMessage {
    private Long orderId;
    private Long supplyStoreId;
    private Long receiveStoreId;
    private Long arrivalHubId;
    private Long departureHubId;
    private String deliveryAddress;
    private String recipient;
}
