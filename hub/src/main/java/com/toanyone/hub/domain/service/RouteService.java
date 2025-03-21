package com.toanyone.hub.domain.service;

import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.presentation.dto.HubDto;
import com.toanyone.hub.presentation.dto.RouteSegmentDto;

import java.util.List;

public interface RouteService {
    List<RouteSegmentDto> findShortestPath(Long startHubId, Long endHubId);

    void addHubDistances(Hub hub);
}
