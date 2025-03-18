package com.toanyone.order.infrastructure;

import com.toanyone.order.application.StoreService;
import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

//Todo: FeignClient 추가하고 삭제 예정
@Service
public class StoreServiceImplTemp implements StoreService {

    @Override
    public boolean validateStore(Long storeId) {
        if (storeId % 2 == 0) {
            return false;
        }
        return true;
    }

}