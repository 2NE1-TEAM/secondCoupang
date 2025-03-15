package com.toanyone.order.application;

import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.dto.ItemValidationResponseDto;
import org.springframework.stereotype.Service;

@Service
public interface ItemService {
    ItemValidationResponseDto validateItems(ItemValidationRequestDto request);

}
