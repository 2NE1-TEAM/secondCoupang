package com.toanyone.order.application.service;

import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import org.springframework.http.ResponseEntity;

public interface ItemService {
    ResponseEntity<Void> validateItems(ItemValidationRequestDto request);

}
