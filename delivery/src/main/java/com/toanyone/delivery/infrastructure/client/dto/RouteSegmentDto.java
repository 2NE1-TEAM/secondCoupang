package com.toanyone.delivery.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RouteSegmentDto implements Serializable {
    private HubDto startHub;
    private HubDto endHub;
    private int distanceKm;
    private int estimatedTime;


}