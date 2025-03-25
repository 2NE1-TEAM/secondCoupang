package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.service.ItemService;
import com.toanyone.order.common.config.FeignConfig;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "item-service", configuration = FeignConfig.class)
public interface ItemClient extends ItemService {

    @PostMapping("/items/adjust-stock")
    ResponseEntity<Void> validateItems(@RequestBody @Valid ItemValidationRequestDto request);

}
