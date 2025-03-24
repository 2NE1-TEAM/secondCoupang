package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.HubService;
import com.toanyone.order.application.dto.HubFindResponseDto;
import com.toanyone.order.common.SingleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hub")
public interface HubClient extends HubService {

    @GetMapping("/hubs/{hubId}")
    SingleResponse<HubFindResponseDto> getHub(@PathVariable("hubId") Long hubId);

}
