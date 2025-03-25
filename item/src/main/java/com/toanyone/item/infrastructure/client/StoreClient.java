package com.toanyone.item.infrastructure.client;

import com.toanyone.item.common.config.feignConfig.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "store-service", configuration = FeignConfig.class)
public interface StoreClient {

    @GetMapping(value = "/stores/{storeId}")
    void existStore(@PathVariable("storeId") Long storeId);
}