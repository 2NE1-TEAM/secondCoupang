package com.toanyone.hub.presentation.dto;

import com.toanyone.hub.domain.model.HubDistance;
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

    public static RouteSegmentDto fromEntity(HubDistance hubDistance) {
        return new RouteSegmentDto(
                HubDto.fromEntity(hubDistance.getStartHub()),
                HubDto.fromEntity(hubDistance.getEndHub()),
                hubDistance.getDistanceKm(),
                hubDistance.getEstimatedTime()
        );
    }
}