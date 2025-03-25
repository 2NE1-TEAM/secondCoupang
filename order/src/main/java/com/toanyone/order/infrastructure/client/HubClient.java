package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.service.HubService;
import com.toanyone.order.application.dto.HubFindResponseDto;
import com.toanyone.order.common.config.FeignConfig;
import com.toanyone.order.common.dto.SingleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hub-service", configuration = FeignConfig.class)
public interface HubClient extends HubService {

    @GetMapping("/hubs/{hubId}")
    ResponseEntity<SingleResponse<HubFindResponseDto>> getHub(@PathVariable("hubId") Long hubId);

}
