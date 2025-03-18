package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.StoreService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

//@FeignClient(name = "store", url = "${store.service.url}")
public interface StoreClient extends StoreService {

    @PostMapping("/stores/{storeId}/validate")
    boolean validateStore(@PathVariable("storeId") Long storeId);

}
