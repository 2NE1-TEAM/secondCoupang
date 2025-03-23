package com.toanyone.order.application;

import com.toanyone.order.application.dto.StoreFindResponseDto;
import com.toanyone.order.common.SingleResponse;

public interface StoreService {
    SingleResponse<StoreFindResponseDto> getStore(Long storeId);
}
