package com.toanyone.hub.domain.service;

import com.toanyone.hub.infrastructure.messaging.dto.HubCreateMessage;
import com.toanyone.hub.presentation.dto.RouteSegmentDto;

import java.util.List;

public interface RouteService {
    List<RouteSegmentDto> findShortestPath(Long startHubId, Long endHubId);

    void addHubDistances(HubCreateMessage hubCreateMessage);
}
