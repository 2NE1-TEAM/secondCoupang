package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.StoreService;
import com.toanyone.order.application.dto.StoreFindResponseDto;
import com.toanyone.order.common.SingleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "store-service")
public interface StoreClient extends StoreService {

    @GetMapping("/stores/{storeId}")
    SingleResponse<StoreFindResponseDto> getStore(@PathVariable("storeId") Long storeId);

}
