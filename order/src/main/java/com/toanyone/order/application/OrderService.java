package com.toanyone.order.application;

import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.dto.ItemValidationResponseDto;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.entity.OrderItem;
import com.toanyone.order.domain.repository.OrderRepository;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.response.OrderCreateResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemService itemService;

    @Transactional
    public OrderCreateResponseDto createOrder(OrderCreateRequestDto request) {

        //Todo: Store 검증 관련 작업 추가

        Order order = Order.create(request.getUserId(), request.getSupplyStoreId(), request.getSupplyStoreId());

        //Item 검증
        ItemValidationRequestDto validItemRequestDto = convertToItemValidationRequestDto(request.getItems());
        ItemValidationResponseDto validItems = itemService.validateItems(validItemRequestDto);

        validItems.getItems().stream().map(validRequest ->
                        OrderItem.create(validRequest.getItemId(), validRequest.getItemName(), validRequest.getOrderedQuantity(), validRequest.getOrderedPrice()))
                .forEach(order::addOrderItem);


        order.calculateTotalPrice();

        log.debug("order - id: {}, userId: {}, totalPrice: {}", order.getId(), order.getUserId(), order.getTotalPrice());

        orderRepository.save(order);

        //Todo: Delivery 관련 작업 추가

        return OrderCreateResponseDto.fromOrder(order);
    }


    //Todo: ItemClient 통해서 Item 검증 후 가능한 Item 응답 받음 (mapper 위치 고민)
    public ItemValidationRequestDto convertToItemValidationRequestDto(List<OrderCreateRequestDto.ItemRequestDto> orderItemRequestDtos) {
        List<ItemValidationRequestDto.ItemRequestDto> itemValidationRequestDtos = orderItemRequestDtos.stream()
                .map(item -> new ItemValidationRequestDto.ItemRequestDto(item.getItemId(), item.getQuantity()))
                .collect(Collectors.toList());

        return new ItemValidationRequestDto(itemValidationRequestDtos);
    }


}
