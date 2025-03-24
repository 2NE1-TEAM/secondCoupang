package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.HubService;
import com.toanyone.order.application.dto.HubFindResponseDto;
import com.toanyone.order.common.SingleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hub-service")
public interface HubClient extends HubService {

    @GetMapping("/hubs/{hubId}")
    ResponseEntity<SingleResponse<HubFindResponseDto>> getHub(@PathVariable("hubId") Long hubId);

}
