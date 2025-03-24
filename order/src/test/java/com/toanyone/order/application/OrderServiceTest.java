package com.toanyone.order.application;

import com.toanyone.order.application.dto.*;
import com.toanyone.order.application.dto.request.OrderCancelServiceDto;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.application.mapper.ItemRequestMapper;
import com.toanyone.order.application.mapper.MessageConverter;
import com.toanyone.order.common.SingleResponse;
import com.toanyone.order.common.UserContext;
import com.toanyone.order.common.exception.OrderException;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.entity.OrderItem;
import com.toanyone.order.domain.repository.OrderItemRepository;
import com.toanyone.order.domain.repository.OrderRepository;
import com.toanyone.order.message.DeliveryRequestMessage;
import com.toanyone.order.message.PaymentCancelMessage;
import com.toanyone.order.message.PaymentRequestMessage;
import com.toanyone.order.presentation.dto.request.OrderCancelRequestDto;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.response.OrderCancelResponseDto;
import com.toanyone.order.presentation.dto.response.OrderCreateResponseDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ItemService itemService;

    @Mock
    private StoreService storeService;

    @Mock
    private HubService hubService;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @Mock
    private MessageConverter messageConverter;

    @Mock
    private OrderKafkaProducer orderKafkaProducer;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OrderService orderService;

    private OrderCreateServiceDto orderRequestDto;
    private ItemValidationRequestDto itemValidationRequestDto;
    private ItemRestoreRequestDto itemRestoreRequestDto;
    private OrderCancelServiceDto cancelRequestDto;
    private Order order;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private SingleResponse<StoreFindResponseDto> supplyStore;
    private SingleResponse<StoreFindResponseDto> receiveStore;
    private SingleResponse<HubFindResponseDto> hub;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {

        order = Order.create(1L, "ordererName", "request", 1L, 2L);
        Field idField = Order.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(order, 1L);

        orderItem1 = OrderItem.create(1L, "Item 1", 2, 1000);
        orderItem2 = OrderItem.create(2L, "Item 2", 1, 2000);

        supplyStore = SingleResponse.success(StoreFindResponseDto.builder()
                .storeId(1L)
                .hubId(1L)
                .build()
        );

        receiveStore = SingleResponse.success(StoreFindResponseDto.builder()
                .storeId(1L)
                .hubId(1L)
                .build()
        );

        hub = SingleResponse.success(HubFindResponseDto.builder()
                        .hubName("hubName")
                        .createdBy(1L)
                .build()
        );

        //주문 생성 요청 데이터
        orderRequestDto = OrderCreateServiceDto.builder()
                .supplyStoreId(1L)
                .receiveStoreId(2L)
                .ordererName("ordererName")
                .request("request")
                .deliveryInfo(OrderCreateServiceDto.DeliveryRequestDto.builder()
                        .deliveryAddress("deliveryAddress")
                        .recipient("recipient")
                        .build())
                .items(List.of(
                        OrderCreateServiceDto.ItemRequestDto.builder()
                                .itemId(1L)
                                .itemName("itemName1")
                                .price(10000)
                                .quantity(10)
                                .build(),
                        OrderCreateServiceDto.ItemRequestDto.builder()
                                .itemId(2L)
                                .itemName("itemName2")
                                .price(20000)
                                .quantity(20)
                                .build()
                ))
                .build();

        //상품 검증 요청 데이터
        itemValidationRequestDto = ItemValidationRequestDto.builder()
                .items(List.of(
                        ItemValidationRequestDto.ItemRequestDto.builder()
                                .itemId(1L)
                                .quantity(10)
                                .build(),
                        ItemValidationRequestDto.ItemRequestDto.builder()
                                .itemId(2L)
                                .quantity(20)
                                .build()
                ))
                .build();

        //재고 복구 요청 데이터
        itemRestoreRequestDto = ItemRestoreRequestDto.builder()
                .items(List.of(
                        ItemRestoreRequestDto.ItemRequestDto.builder()
                                .itemId(1L)
                                .quantity(10)
                                .build(),
                        ItemRestoreRequestDto.ItemRequestDto.builder()
                                .itemId(2L)
                                .quantity(20)
                                .build()
                ))
                .build();

        //주문 취소 요청 데이터
        cancelRequestDto = OrderCancelServiceDto.builder()
                .orderId(1L)
                .deliveryId(1L)
                .build();

    }

    @Test
    @DisplayName("주문 생성 성공 - Store Feign Client 호출 확인")
    void createOrderSuccess() {
        //given
        when(storeService.getStore(1L)).thenReturn(supplyStore);

        when(storeService.getStore(2L)).thenReturn(receiveStore);

        when(itemService.validateItems(any())).thenReturn(true);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(savedOrder, 1L);
            return savedOrder;
        });

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(messageConverter.toOrderDeliveryMessage(any(), anyLong(), anyLong(), anyLong())).thenReturn(new DeliveryRequestMessage());
        when(messageConverter.toOrderPaymentMessage(anyLong(), anyInt())).thenReturn(new PaymentRequestMessage());

        //when
        OrderCreateResponseDto responseDto = orderService.createOrder(1L, "MASTER", "slackId", orderRequestDto);

        //then
        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getOrderId());

        verify(storeService, times(1)).getStore(1L);
        verify(storeService, times(1)).getStore(2L);

    }

    @Test
    @DisplayName("주문 취소 성공 - MASTER 권한")
    void cancelOrderSuccessByMaster() {

        //given
        Long orderId = 1L;
        Long userId = 1L;
        String role = "MASTER";
        String slackId = "slackId";
        order.completedPayment();

        when(orderRepository.findById(orderId)).thenReturn(Optional.ofNullable(order));

        //when
        OrderCancelResponseDto responseDto = orderService.cancelOrder(userId, role, slackId, cancelRequestDto);

        //then
        assertEquals(Order.OrderStatus.PAYMENT_CANCEL_REQUESTED, order.getStatus());
        verify(orderKafkaProducer, times(1)).sendPaymentCancelMessage(
                any(PaymentCancelMessage.class), eq(userId), eq(role), eq(slackId)
        );
        assertNotNull(responseDto);
        assertEquals(orderId, responseDto.getOrderId());

    }


    @Test
    @DisplayName("주문 취소 성공 - HUB 권한")
    void cancelOrderSuccessByHubManager() {

        //given
        Long orderId = 1L;
        Long userId = 1L;
        String role = "HUB";
        String slackId = "slackId";
        order.completedPayment();

        when(storeService.getStore(1L)).thenReturn(supplyStore);
        when(hubService.getHub(1L)).thenReturn(hub);
        when(orderRepository.findById(orderId)).thenReturn(Optional.ofNullable(order));

        //when
        OrderCancelResponseDto responseDto = orderService.cancelOrder(userId, role, slackId, cancelRequestDto);

        //then
        assertEquals(Order.OrderStatus.PAYMENT_CANCEL_REQUESTED, order.getStatus());
        verify(orderKafkaProducer, times(1)).sendPaymentCancelMessage(
                any(PaymentCancelMessage.class), eq(userId), eq(role), eq(slackId)
        );
        assertNotNull(responseDto);
        assertEquals(orderId, responseDto.getOrderId());

    }


    @Test
    @DisplayName("주문 취소 실패")
    void cancelOrderFail() {

        //given
        Long orderId = 1L;
        Long userId = 1L;
        String role = "MASTER";
        String slackId = "slackId";

        when(orderRepository.findById(orderId)).thenReturn(Optional.ofNullable(order));

        //when&then
        Assertions.assertThrows(OrderException.OrderCancelFailedException.class, () -> {
            orderService.cancelOrder(userId, role, slackId, cancelRequestDto);
        });

        verify(orderKafkaProducer, times(0)).sendPaymentCancelMessage(
                any(PaymentCancelMessage.class), eq(userId), eq(role), eq(slackId)
        );

    }

    @Test
    @DisplayName("결제 성공 시 주문 상태 업데이트 성공")
    void updateOrderStatusSuccessWhenPaymentSuccess() {

        //given
        Long orderId = 1L;
        String status = "PAYMENT_SUCCESS";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(String.valueOf(orderId))).thenReturn(new DeliveryRequestMessage());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        //when
        DeliveryRequestMessage deliveryMessage = orderService.processDeliveryRequest(orderId, status);

        //then
        assertNotNull(deliveryMessage);
        assertEquals(Order.OrderStatus.PREPARING, order.getStatus());

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("결제 성공 시 주문 상태 업데이트 실패")
    void updateOrderStatusFailedWhenPaymentSuccess() {

        //given
        Long orderId = 1L;
        String status = "PAYMENT_FAILED";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(String.valueOf(orderId))).thenReturn(new DeliveryRequestMessage());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        //when
        DeliveryRequestMessage deliveryMessage = orderService.processDeliveryRequest(orderId, status);

        //then
        assertNotNull(deliveryMessage);
        assertNotEquals(Order.OrderStatus.PREPARING, order.getStatus());
        assertEquals(Order.OrderStatus.PAYMENT_WAITING, order.getStatus());

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("배송 시작 시 주문 상태 업데이트 성공")
    void updateOrderStatusSuccessWhenDeliverySuccess() {

        //given
        Long orderId = 1L;
        String status = "DELIVERING";
        order.completedPayment();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        //when
        orderService.processDeliverySuccessRequest(orderId, status);

        //then
        assertEquals(Order.OrderStatus.DELIVERING, order.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("배송 시작 시 주문 상태 업데이트 실패")
    void updateOrderStatusFailedWhenDeliverySuccess() {

        //given
        Long orderId = 1L;
        String status = "DELIVERING";
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        //when&then
        assertThrows(OrderException.OrderStatusIllegalException.class, () -> {
            orderService.processDeliverySuccessRequest(orderId, status);
        });

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("배송 완료 시 주문 상태 업데이트 성공")
    void updateOrderStatusSuccessWhenDeliveryUpdatedSuccess() {

        //given
        Long orderId = 1L;
        String status = "DELIVERY_COMPLETED";
        order.completedPayment();
        order.startDelivery();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        //when
        orderService.processDeliveryUpdatedRequest(orderId, status);

        //then
        assertEquals(Order.OrderStatus.DELIVERY_COMPLETED, order.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("주문 취소 시 주문 상태 업데이트 성공")
    void updateOrderStatusSuccessWhenOrderCancel() {

        //given
        Long orderId = 1L;
        String status = "CANCELED";

        order.addOrderItem(orderItem1);
        order.addOrderItem(orderItem2);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(itemService.restoreInventory(any())).thenReturn(true);

        //when
        orderService.processOrderCancellation(orderId, status);

        //then
        assertEquals(Order.OrderStatus.CANCELED, order.getStatus());
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
        verify(itemService, times(1)).restoreInventory(any());
        verify(orderItemRepository, times(1)).bulkUpdateOrderItemsStatus(orderId, OrderItem.OrderItemStatus.CANCELED);
    }

    @Test
    @DisplayName("주문 취소 시 주문 상태 업데이트 실패")
    void updateOrderStatusFailWhenOrderCancel() {

        //given
        Long orderId = 1L;
        String status = "CANCELED";

        order.completedPayment();
        order.startDelivery();
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        //then&when
        assertThrows(OrderException.OrderStatusIllegalException.class, () -> {
            orderService.processOrderCancellation(orderId, status);
        });

        verify(orderRepository, times(1)).findByIdWithItems(orderId);
    }

}