package com.toanyone.order.infrastructure;

import com.toanyone.order.application.ItemService;
import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

//Todo: FeignClient 추가하고 삭제 예정
@Service
public class ItemServiceImplTemp implements ItemService {

    @Override
    public boolean validateItems(@Valid ItemValidationRequestDto request) {
        return true;
    }

    @Override
    public boolean restoreInventory(ItemRestoreRequestDto request) {
        return true;
    }
}
