package com.toanyone.delivery.application.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetDeliveryManagerSearchConditionRequestDto {
    private Long deliveryManagerId;
    private String deliveryManagerType;
    private Long userId;
    private String name;
    private String sortBy;
    private int limit;
}
