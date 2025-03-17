package com.toanyone.store.infrastructure.client;

import com.toanyone.store.infrastructure.client.dto.HubResponseDto;
import com.toanyone.store.presentation.dto.SingleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hub-service", url = "http://localhost:9000")
public interface HubClient {

    @GetMapping("/hubs/{hubId}")
    ResponseEntity<SingleResponse<HubResponseDto>> getHubById(@PathVariable("hubId") Long hubId);
}