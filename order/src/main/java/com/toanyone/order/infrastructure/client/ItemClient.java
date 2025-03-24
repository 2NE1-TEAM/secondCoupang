package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.service.ItemService;
import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.common.config.FeignConfig;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "item-service", configuration = FeignConfig.class)
public interface ItemClient extends ItemService {

    @PostMapping("/items/validate")
    boolean validateItems(@RequestBody @Valid ItemValidationRequestDto request);

    @PostMapping("/items/restore")
    boolean restoreInventory(@RequestBody @Valid ItemRestoreRequestDto request);

}
