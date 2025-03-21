package com.toanyone.order.application;

import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.dto.StoreFindResponseDto;
import com.toanyone.order.common.SingleResponse;
import org.springframework.web.bind.annotation.PathVariable;

public interface StoreService {
    boolean validateStore(Long storeId);
    SingleResponse<StoreFindResponseDto> getStore(Long storeId);
}
