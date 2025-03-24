package com.toanyone.order.application;

import com.toanyone.order.application.dto.StoreFindResponseDto;
import com.toanyone.order.common.SingleResponse;
import org.springframework.http.ResponseEntity;

public interface StoreService {
    ResponseEntity<SingleResponse<StoreFindResponseDto>> getStore(Long storeId);
}
