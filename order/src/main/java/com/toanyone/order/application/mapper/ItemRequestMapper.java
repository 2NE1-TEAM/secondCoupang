package com.toanyone.order.application.mapper;

import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.domain.model.Order;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class ItemRequestMapper {

    public ItemValidationRequestDto toItemValidationRequestDto(OrderCreateServiceDto orderCreateRequest, String type) {

        return new ItemValidationRequestDto(
                type,
                orderCreateRequest.getItems().stream()
                .map(orderItem ->
                        new ItemValidationRequestDto.ItemRequestDto(orderItem.getItemId(), orderItem.getQuantity())
                ).collect(Collectors.toList())
        );
    }


    public ItemValidationRequestDto toItemRestoreDto(Order order, String type) {
        return new ItemValidationRequestDto(
                type,
                order.getItems().stream()
                .map(orderItem -> new ItemValidationRequestDto.ItemRequestDto(orderItem.getItemId(), orderItem.getQuantity())
                ).collect(Collectors.toList()));
    }

}
