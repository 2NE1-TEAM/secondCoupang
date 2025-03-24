package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.service.ItemService;
import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "item-service")
public interface ItemClient extends ItemService {

    @PostMapping("/items/validate")
    boolean validateItems(@RequestBody @Valid ItemValidationRequestDto request);

    @PostMapping("/items/restore")
    boolean restoreInventory(@RequestBody @Valid ItemRestoreRequestDto request);

}
