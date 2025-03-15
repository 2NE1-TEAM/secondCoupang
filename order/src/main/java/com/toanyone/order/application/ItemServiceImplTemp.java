package com.toanyone.order.application;

import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.dto.ItemValidationResponseDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

//Todo: FeignClient 추가하고 삭제 예정
@Service
public class ItemServiceImplTemp implements ItemService{

    @Override
    public boolean validateItems(@Valid ItemValidationRequestDto request) {

        return true;
    }
}