package com.toanyone.delivery.infrastructure.client.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class HubFindResponseDto {
    private Long hubId;
    private String hubName;
    private Address address;
    private Location location;
    private String telephone;

    @Getter
    public static class Address {
        private String address;
    }

    @Getter
    public static class Location {
        private BigDecimal latitude;  // 위도

        private BigDecimal longitude; // 경도
    }
}
