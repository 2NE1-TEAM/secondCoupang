package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.ItemService;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.dto.ItemValidationResponseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "item", url = "${item.service.url}")
public interface ItemClient extends ItemService {

    @PostMapping("/items/validate")
    ItemValidationResponseDto validateItems(@RequestBody ItemValidationRequestDto request);

}
