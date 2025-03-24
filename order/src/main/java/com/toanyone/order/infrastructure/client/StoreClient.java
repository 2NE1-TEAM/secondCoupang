package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.service.StoreService;
import com.toanyone.order.application.dto.StoreFindResponseDto;
import com.toanyone.order.common.dto.SingleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "store-service")
public interface StoreClient extends StoreService {

    @GetMapping("/stores/{storeId}")
    ResponseEntity<SingleResponse<StoreFindResponseDto>> getStore(@PathVariable("storeId") Long storeId);

}
