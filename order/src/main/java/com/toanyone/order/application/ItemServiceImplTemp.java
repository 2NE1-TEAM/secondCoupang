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
    public ItemValidationResponseDto validateItems(@Valid ItemValidationRequestDto request) {

        List<ItemValidationResponseDto.ItemResponseDto> validateItems = request.getItems().stream()
                .map(item -> new ItemValidationResponseDto.ItemResponseDto(item.getItemId(), "itemName", 10000, item.getQuantity()))
                .collect(Collectors.toList());

        return new ItemValidationResponseDto(validateItems);
    }
}