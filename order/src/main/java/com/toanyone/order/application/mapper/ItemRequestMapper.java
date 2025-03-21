package com.toanyone.order.application.mapper;

import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
public class ItemRequestMapper {

    public ItemValidationRequestDto toItemValidationRequestDto(OrderCreateServiceDto orderCreateRequest) {

        return new ItemValidationRequestDto(orderCreateRequest.getItems().stream()
                .map(orderItem ->
                        new ItemValidationRequestDto.ItemRequestDto(orderItem.getItemId(), orderItem.getQuantity())
                ).collect(Collectors.toList())
        );
    }


    public ItemRestoreRequestDto toItemRestoreDto(Order order) {
        return new ItemRestoreRequestDto(order.getItems().stream()
                .map(orderItem -> new ItemRestoreRequestDto.ItemRequestDto(orderItem.getItemId(), orderItem.getQuantity())
                ).collect(Collectors.toList()));
    }

}
