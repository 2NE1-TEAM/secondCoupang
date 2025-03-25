package com.toanyone.hub.presentation.controller;

import com.toanyone.hub.common.config.annotation.RequireRole;
import com.toanyone.hub.common.filter.UserContext;
import com.toanyone.hub.domain.service.HubService;
import com.toanyone.hub.domain.service.RouteService;
import com.toanyone.hub.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/hubs")
public class HubController {

    private final HubService hubService;
    private final RouteService routeService;
    private final UserContext userContext;

    /**
     * 허브 생성 및 생성 후 HubDistance에 경로 추가 비동기로 실행.
     */
    @RequireRole("MASTER")
    @PostMapping
    public ResponseEntity createHub(@RequestBody @Valid HubCreateRequestDto hubCreateRequestDto) {
        log.info("HubCreateRequestDto:{}", hubCreateRequestDto);
        HubCreateResponseDto hubCreateResponseDto = hubService.createHub(hubCreateRequestDto);
        return ResponseEntity.ok(SingleResponse.success(hubCreateResponseDto));
    }

    /**
     * 단건 조회
     */
    @GetMapping("/{hubId}")
    public ResponseEntity findHub(@PathVariable Long hubId) {
        log.info("HubId:{}", hubId);
        return ResponseEntity.ok(SingleResponse.success(hubService.findOne(hubId)));
    }

    /**
     * 다건 복수조회 및 검색
     */
    @GetMapping
    public ResponseEntity findAllStore(
            @ModelAttribute HubSearchRequest searchRequest,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        log.info("searchRequest:{}", searchRequest);
        CursorPage<HubFindResponseDto> stores = hubService.findHubs(searchRequest, sortBy, direction, size);
        return ResponseEntity.ok(MultiResponse.success(stores));
    }

    /**
     * 최적경로 검색
     */
    @GetMapping("/route")
    public ResponseEntity findHub(@RequestParam Long startHubId, @RequestParam Long endHubId) {
        log.info("findHub startHubId: {}, endHubId: {}", startHubId, endHubId);
        List<RouteSegmentDto> shortestPath = routeService.findShortestPath(startHubId, endHubId);
        return ResponseEntity.ok(SingleResponse.success(shortestPath));
    }

    /**
     * 허브 삭제
     */
    @RequireRole("MASTER")
    @DeleteMapping("/{hubId}")
    public ResponseEntity deleteHub(@PathVariable Long hubId) {
        log.info("deleteHub:{}", hubId);
        hubService.deleteHub(hubId, userContext.getUser().getUserId());
        return ResponseEntity.noContent().build();
    }

    @RequireRole("MASTER")
    @PatchMapping("/{hubId}")
    public ResponseEntity updateHub(@PathVariable Long hubId, @RequestBody HubUpdateRequestDto requestDto) {
        return ResponseEntity.ok(SingleResponse.success(hubService.updateHub(hubId, requestDto)));
    }
}
