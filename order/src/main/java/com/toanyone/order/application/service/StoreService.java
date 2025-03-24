package com.toanyone.order.application.service;

import com.toanyone.order.application.dto.StoreFindResponseDto;
import com.toanyone.order.common.dto.SingleResponse;
import org.springframework.http.ResponseEntity;

public interface StoreService {
    ResponseEntity<SingleResponse<StoreFindResponseDto>> getStore(Long storeId);
}
