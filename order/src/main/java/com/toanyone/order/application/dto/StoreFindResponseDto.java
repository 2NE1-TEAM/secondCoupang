package com.toanyone.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreFindResponseDto {
    private Long storeId;
    private String storeName;
    private Long hubId;
    private String hubName;
    private String telephone;
}