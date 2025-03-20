package com.toanyone.order.application;

import com.toanyone.order.application.dto.ItemValidationRequestDto;
import org.springframework.web.bind.annotation.PathVariable;

public interface StoreService {
    boolean validateStore(Long storeId);

}
