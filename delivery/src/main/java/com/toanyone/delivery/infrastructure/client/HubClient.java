package com.toanyone.delivery.infrastructure.client;

import com.toanyone.delivery.common.utils.SingleResponse;
import com.toanyone.delivery.infrastructure.client.dto.HubFindResponseDto;
import com.toanyone.delivery.infrastructure.client.dto.RouteSegmentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "hub-service")
public interface HubClient {

    @GetMapping("/hubs/route")
    ResponseEntity<SingleResponse<List<RouteSegmentDto>>> findHub(@RequestParam Long startHubId, @RequestParam Long endHubId);

    @GetMapping("/hubs/{hubId}")
    ResponseEntity<SingleResponse<HubFindResponseDto>> getHubById(@PathVariable("hubId") Long hubId);
}
