package com.toanyone.store.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Location {

    @Column(nullable = false, precision = 10, scale = 7) // 최대 10자리, 소수점 7자리
    private BigDecimal latitude;  // 위도

    @Column(nullable = false, precision = 10, scale = 7) // 최대 10자리, 소수점 7자리
    private BigDecimal longitude; // 경도
}