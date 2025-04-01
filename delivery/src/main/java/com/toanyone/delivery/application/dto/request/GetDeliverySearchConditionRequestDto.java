package com.toanyone.delivery.application.dto.request;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class GetDeliverySearchConditionRequestDto {

    private Long deliveryId;
    private String deliveryStatus;
    private Long departureHubId;
    private Long arrivalHubId;
    private String recipient;
    private Long storeDeliveryManagerId;
    private int limit;
    private String sortBy;
}
