package com.toanyone.delivery.application.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetDeliveryManagerSearchConditionRequestDto {
    @JsonProperty("delivery_manager_id")
    private Long deliveryManagerId;
    @JsonProperty("delivery_manager_type")
    private String deliveryManagerType;
    @JsonProperty("sort_by")
    private String sortBy;
    private int limit;
}
