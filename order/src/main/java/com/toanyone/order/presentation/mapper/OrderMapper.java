package com.toanyone.order.presentation.mapper;

import com.toanyone.order.application.dto.request.OrderCancelServiceDto;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.application.dto.request.OrderFindAllCondition;
import com.toanyone.order.application.dto.request.OrderSearchCondition;
import com.toanyone.order.presentation.dto.request.OrderCancelRequestDto;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.request.OrderFindAllRequestDto;
import com.toanyone.order.presentation.dto.request.OrderSearchRequestDto;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;


@Component
public class OrderMapper {

    public OrderCreateServiceDto toOrderCreateServiceDto(OrderCreateRequestDto request) {
        return OrderCreateServiceDto.builder()
                .supplyStoreId(request.getSupplyStoreId())
                .receiveStoreId(request.getReceiveStoreId())
                .ordererName(request.getOrdererName())
                .request(request.getRequest())
                .items(request.getItems().stream()
                        .map(itemRequestDto -> OrderCreateServiceDto.ItemRequestDto.builder()
                                .itemId(itemRequestDto.getItemId())
                                .itemName(itemRequestDto.getItemName())
                                .quantity(itemRequestDto.getQuantity())
                                .price(itemRequestDto.getPrice())
                                .build())
                        .collect(Collectors.toList()))
                .deliveryInfo(OrderCreateServiceDto.DeliveryRequestDto.builder()
                        .deliveryAddress(request.getDeliveryInfo().getDeliveryAddress())
                        .recipient(request.getDeliveryInfo().getRecipient())
                        .build())
                .build();
    }

    public OrderCancelServiceDto toOrderCancelServiceDto(Long orderId, OrderCancelRequestDto request) {
        return OrderCancelServiceDto.builder()
                .orderId(orderId)
                .deliveryId(request.getDeliveryId())
                .build();
    }

    public OrderSearchCondition toOrderSearchCondition(OrderSearchRequestDto request) {
        return OrderSearchCondition.builder()
                .size(request.getSize())
                .hubId(request.getHubId())
                .userId(request.getUserId())
                .cursorId(request.getCursorId())
                .keyword(request.getKeyword())
                .sortType(request.getSortType())
                .storeId(request.getStoreId())
                .timestamp(request.getTimestamp())
                .build();
    }

    public OrderFindAllCondition toOrderFindAllCondition(OrderFindAllRequestDto request) {
        return OrderFindAllCondition.builder()
                .size(request.getSize())
                .cursorId(request.getCursorId())
                .sortType(request.getSortType())
                .timestamp(request.getTimestamp())
                .build();
    }

}





