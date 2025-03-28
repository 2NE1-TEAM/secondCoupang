package com.toanyone.order.application.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubFindResponseDto {

    private Long hubId;
    private String hubName;
    private Address address;
    private Location location;
    private String telephone;
    private Long createdBy; //hub 담당자 임시

    @Getter
    @Builder
    public static class Address {
        private String address;
    }

    @Getter
    @Builder
    public static class Location {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

}