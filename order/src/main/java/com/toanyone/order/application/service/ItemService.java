package com.toanyone.order.application.service;

import com.toanyone.order.application.dto.service.request.ItemValidationRequestDto;
import org.springframework.http.ResponseEntity;

public interface ItemService {
    ResponseEntity<Void> validateItems(ItemValidationRequestDto request);

}
