package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.StoreService;
import com.toanyone.order.application.dto.StoreFindResponseDto;
import com.toanyone.order.common.SingleResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

//@FeignClient(name = "store", url = "${store.service.url}")
public interface StoreClient extends StoreService {

//    @PostMapping("/stores/{storeId}/validate")
//    boolean validateStore(@PathVariable("storeId") Long storeId);

    @GetMapping("/stores/{storeId}")
    SingleResponse<StoreFindResponseDto> getStore(@PathVariable("storeId") Long storeId);

}
