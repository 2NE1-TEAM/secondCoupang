package com.toanyone.order.application;

import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.dto.ItemValidationResponseDto;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.repository.OrderRepository;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.response.OrderCreateResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ItemService itemService;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderCreateRequestDto orderRequestDto;
    private ItemValidationRequestDto itemValidationRequestDto;
    private ItemValidationResponseDto itemResponseDto;
    private Order order;

    @BeforeEach
    void setUp() {

        order = Order.create(1L, 1L, 2L);

        //주문 생성 요청
        OrderCreateRequestDto.ItemRequestDto itemRequestDto1 = new OrderCreateRequestDto.ItemRequestDto(1L, 10);
        OrderCreateRequestDto.ItemRequestDto itemRequestDto2 = new OrderCreateRequestDto.ItemRequestDto(2L, 20);
        OrderCreateRequestDto.ItemRequestDto itemRequestDto3 = new OrderCreateRequestDto.ItemRequestDto(3L, 30);
        List<OrderCreateRequestDto.ItemRequestDto> items = new ArrayList<>();
        items.add(itemRequestDto1);
        items.add(itemRequestDto2);
        items.add(itemRequestDto3);
        OrderCreateRequestDto.DeliveryRequestDto deliveryRequestDto = new OrderCreateRequestDto.DeliveryRequestDto("deliveryAddress", "recipient");

        orderRequestDto = new OrderCreateRequestDto(1L, 2L, 3L, items, deliveryRequestDto);

        //상품 검증 요청
        ItemValidationRequestDto.ItemRequestDto itemValidationRequestDto1 = new ItemValidationRequestDto.ItemRequestDto(1L, 10);
        ItemValidationRequestDto.ItemRequestDto itemValidationRequestDto2 = new ItemValidationRequestDto.ItemRequestDto(2L, 20);
        ItemValidationRequestDto.ItemRequestDto itemValidationRequestDto3 = new ItemValidationRequestDto.ItemRequestDto(3L, 30);
        List<ItemValidationRequestDto.ItemRequestDto> validItems = new ArrayList<>();
        validItems.add(itemValidationRequestDto1);
        validItems.add(itemValidationRequestDto2);
        validItems.add(itemValidationRequestDto3);

        itemValidationRequestDto = new ItemValidationRequestDto(validItems);

        //검증된 상품 응답
        ItemValidationResponseDto.ItemResponseDto itemValidationResponseDto1 = new ItemValidationResponseDto.ItemResponseDto(1L, "one", 1000, 10);
        ItemValidationResponseDto.ItemResponseDto itemValidationResponseDto2 = new ItemValidationResponseDto.ItemResponseDto(2L, "two", 2000, 20);
        ItemValidationResponseDto.ItemResponseDto itemValidationResponseDto3 = new ItemValidationResponseDto.ItemResponseDto(3L, "three", 3000, 30);
        List<ItemValidationResponseDto.ItemResponseDto> validResponseItems = new ArrayList<>();
        validResponseItems.add(itemValidationResponseDto1);
        validResponseItems.add(itemValidationResponseDto2);
        validResponseItems.add(itemValidationResponseDto3);

        itemResponseDto = new ItemValidationResponseDto(validResponseItems);

    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrderSuccess() {

        //given
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(itemService.validateItems(any(ItemValidationRequestDto.class))).thenReturn(itemResponseDto);

        //when
        OrderCreateResponseDto responseDto = orderService.createOrder(orderRequestDto);

        int itemCount = responseDto.getOrderItemIds().size();

        Assertions.assertEquals(3, itemCount);

    }


}