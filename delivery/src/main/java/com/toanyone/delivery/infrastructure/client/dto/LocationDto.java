package com.toanyone.delivery.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto implements Serializable {
    private BigDecimal latitude;
    private BigDecimal longitude;
}