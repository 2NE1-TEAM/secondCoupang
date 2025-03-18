package com.toanyone.delivery.application.dtos.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetDeliveryManagerSearchConditionRequestDto {
    private Long deliveryManagerId;
    private String deliveryManagerType;
    private String sortBy;
    private int limit;
}
