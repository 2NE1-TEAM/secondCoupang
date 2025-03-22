package com.toanyone.order.application;

import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.dto.ItemValidationResponseDto;
import com.toanyone.order.application.dto.request.OrderCancelServiceDto;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.application.mapper.ItemRequestMapper;
import com.toanyone.order.common.exception.OrderException;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.repository.OrderRepository;
import com.toanyone.order.presentation.dto.request.OrderCancelRequestDto;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.response.OrderCancelResponseDto;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @Mock
    private ItemService itemService;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderCreateServiceDto orderRequestDto;
    private ItemValidationRequestDto itemValidationRequestDto;
    private ItemRestoreRequestDto itemRestoreRequestDto;
    private OrderCancelServiceDto cancelRequestDto;
    private Order order;

    @BeforeEach
    void setUp() {

        order = Order.create(1L, "ordererName", "12시 전에",1L, 2L);

        //주문 생성 요청
        OrderCreateServiceDto.ItemRequestDto itemRequestDto1 = OrderCreateServiceDto.ItemRequestDto.builder()
                .itemId(1L)
                .itemName("item1")
                .price(1000)
                .quantity(10)
                .build();
        OrderCreateServiceDto.ItemRequestDto itemRequestDto2 = OrderCreateServiceDto.ItemRequestDto.builder()
                .itemId(2L)
                .itemName("item2")
                .price(2000)
                .quantity(20)
                .build();
        OrderCreateServiceDto.ItemRequestDto itemRequestDto3 = OrderCreateServiceDto.ItemRequestDto.builder()
                .itemId(3L)
                .itemName("item3")
                .price(3000)
                .quantity(30)
                .build();
        OrderCreateServiceDto.DeliveryRequestDto deliveryRequestDto = OrderCreateServiceDto.DeliveryRequestDto.builder()
                .recipient("recipient")
                .build();
        List<OrderCreateServiceDto.ItemRequestDto> itemRequests = new ArrayList<>();
        itemRequests.add(itemRequestDto1);
        itemRequests.add(itemRequestDto2);
        itemRequests.add(itemRequestDto3);

        orderRequestDto = OrderCreateServiceDto.builder()
                .supplyStoreId(2L)
                .receiveStoreId(3L)
                .deliveryInfo(deliveryRequestDto)
                .items(itemRequests)
                .build();

        //상품 검증 요청
        ItemValidationRequestDto.ItemRequestDto itemValidationRequestDto1 = new ItemValidationRequestDto.ItemRequestDto(1L, 10);
        ItemValidationRequestDto.ItemRequestDto itemValidationRequestDto2 = new ItemValidationRequestDto.ItemRequestDto(2L, 20);
        ItemValidationRequestDto.ItemRequestDto itemValidationRequestDto3 = new ItemValidationRequestDto.ItemRequestDto(3L, 30);
        List<ItemValidationRequestDto.ItemRequestDto> validItems = new ArrayList<>();
        validItems.add(itemValidationRequestDto1);
        validItems.add(itemValidationRequestDto2);
        validItems.add(itemValidationRequestDto3);

        itemValidationRequestDto = new ItemValidationRequestDto(validItems);


        //재고 취소 요청
        ItemRestoreRequestDto.ItemRequestDto itemRestoreRequestDto1 = new ItemRestoreRequestDto.ItemRequestDto(1L, 10);
        ItemRestoreRequestDto.ItemRequestDto itemRestoreRequestDto2 = new ItemRestoreRequestDto.ItemRequestDto(2L, 20);
        ItemRestoreRequestDto.ItemRequestDto itemRestoreRequestDto3 = new ItemRestoreRequestDto.ItemRequestDto(3L, 30);
        List<ItemRestoreRequestDto.ItemRequestDto> restoreItems = new ArrayList<>();
        restoreItems.add(itemRestoreRequestDto1);
        restoreItems.add(itemRestoreRequestDto2);
        restoreItems.add(itemRestoreRequestDto3);

        itemRestoreRequestDto = new ItemRestoreRequestDto(restoreItems);

        //주문 취소 요청
        cancelRequestDto = OrderCancelServiceDto.builder().orderId(1L).deliveryId(2L).build();

    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrderSuccess() {

        //given
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(itemRequestMapper.toItemValidationRequestDto(orderRequestDto)).thenReturn(itemValidationRequestDto);
        when(itemService.validateItems(any(ItemValidationRequestDto.class))).thenReturn(true);
        int totalPrice = orderRequestDto.getItems().stream().mapToInt(item -> item.getQuantity() * item.getPrice()).sum();


        //when
        OrderCreateResponseDto responseDto = orderService.createOrder(1L, "USER", 2L, orderRequestDto);
        int itemCount = responseDto.getOrderItemIds().size();
        int responseTotalPrice = responseDto.getTotalPrice();

        Assertions.assertEquals(3, itemCount);
        Assertions.assertEquals(totalPrice, responseTotalPrice);

    }

    @Test
    @DisplayName("주문 취소 실패")
    void cancelOrderFail() {

        //given
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.ofNullable(order));
        when(itemRequestMapper.toItemRestoreDto(order)).thenReturn(itemRestoreRequestDto);
        when(itemService.restoreInventory(any(ItemRestoreRequestDto.class))).thenReturn(false);

        //when&then
        Assertions.assertThrows(OrderException.OrderCancelFailedException.class, () -> {
            orderService.cancelOrder(1L, "USER", 1L, cancelRequestDto);
        });

    }

}