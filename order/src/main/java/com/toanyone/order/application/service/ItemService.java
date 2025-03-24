package com.toanyone.order.application.service;

import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;

public interface ItemService {
    boolean validateItems(ItemValidationRequestDto request);

    boolean restoreInventory(ItemRestoreRequestDto request);

}
