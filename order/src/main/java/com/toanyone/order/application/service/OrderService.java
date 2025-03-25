package com.toanyone.order.application.service;

import com.toanyone.order.application.dto.HubFindResponseDto;
import com.toanyone.order.application.dto.SlackMessageRequestDto;
import com.toanyone.order.application.dto.StoreFindResponseDto;
import com.toanyone.order.application.dto.request.OrderCancelServiceDto;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.application.dto.request.OrderFindAllCondition;
import com.toanyone.order.application.dto.request.OrderSearchCondition;
import com.toanyone.order.application.mapper.ItemRequestMapper;
import com.toanyone.order.application.mapper.MessageConverter;
import com.toanyone.order.common.dto.CursorPage;
import com.toanyone.order.common.dto.SingleResponse;
import com.toanyone.order.common.exception.OrderException;
import com.toanyone.order.domain.model.Order;
import com.toanyone.order.domain.model.OrderItem;
import com.toanyone.order.domain.repository.OrderItemRepository;
import com.toanyone.order.domain.repository.OrderRepository;
import com.toanyone.order.message.DeliveryRequestMessage;
import com.toanyone.order.message.PaymentCancelMessage;
import com.toanyone.order.message.PaymentRequestMessage;
import com.toanyone.order.presentation.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j(topic = "OrderService")
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int ORDER_ITEM_MAX = 20;
    private static final int DELIVERY_REQUEST_EXPIRATION_MINUTES = 10;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemService itemService;
    private final StoreService storeService;
    private final HubService hubService;
    private final ItemRequestMapper itemRequestMapper;
    private final MessageConverter messageConverter;
    private final OrderKafkaProducer orderKafkaProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public OrderCreateResponseDto createOrder(Long userId, String role, String slackId, OrderCreateServiceDto request) {

        ResponseEntity<SingleResponse<StoreFindResponseDto>> supplyStore = storeService.getStore(request.getSupplyStoreId());
        ResponseEntity<SingleResponse<StoreFindResponseDto>> receiveStore = storeService.getStore(request.getReceiveStoreId());

        if (supplyStore.getBody().getErrorCode() != null || receiveStore.getBody().getErrorCode() != null) {
            throw new OrderException.InvalidStoreException();
        }

        if (request.getItems().size() > ORDER_ITEM_MAX) {
            throw new OrderException.OrderBadRequestException();
        }

        ResponseEntity<Void> itemResponse = itemService.validateItems(itemRequestMapper.toItemValidationRequestDto(request, "DECREASE"));
        if (itemResponse.getStatusCode() == HttpStatus.BAD_REQUEST){
            throw new OrderException.InsufficientStockException();
        }

        Order order = Order.create(userId, request.getOrdererName(), request.getRequest(),
                request.getSupplyStoreId(), request.getReceiveStoreId());

        request.getItems().stream().map(validRequest ->
                        OrderItem.create(validRequest.getItemId(),
                                validRequest.getItemName(),
                                validRequest.getQuantity(),
                                validRequest.getPrice()))
                .forEach(order::addOrderItem);

        order.calculateTotalPrice();

        orderRepository.save(order);

        log.info("orderId: {}, userId: {}, totalPrice: {}", order.getId(), order.getUserId(), order.getTotalPrice());

        DeliveryRequestMessage deliveryMessage = messageConverter.toOrderDeliveryMessage(request, order.getId(), receiveStore.getBody().getData().getHubId(), supplyStore.getBody().getData().getHubId());

        redisTemplate.opsForValue().set(order.getId().toString(), deliveryMessage, Duration.ofMinutes(DELIVERY_REQUEST_EXPIRATION_MINUTES));

        PaymentRequestMessage paymentMessage = messageConverter.toOrderPaymentMessage(order.getId(), order.getTotalPrice());
        orderKafkaProducer.sendPaymentRequestMessage(paymentMessage, userId, role, slackId);

        return OrderCreateResponseDto.fromOrder(order);
    }

    @Transactional(readOnly = true)
    public OrderFindResponseDto findOrder(Long orderId) {
        Order order = validateOrderExists(orderId);
        return OrderFindResponseDto.fromOrder(order);
    }


    @Transactional(readOnly = true)
    public CursorPage<OrderFindAllResponseDto> findOrders(Long userId, OrderFindAllCondition request) {
        CursorPage<Order> orders = orderRepository.findAll(userId, request);

        List<OrderFindAllResponseDto> responseDtos = orders.getContent().stream().map(OrderFindAllResponseDto::fromOrder).collect(Collectors.toList());

        return new CursorPage<>(responseDtos, orders.getNextCursor(), orders.isHasNext());
    }

    @Transactional(readOnly = true)
    public CursorPage<OrderSearchResponseDto> searchOrders(OrderSearchCondition request) {
        log.info("searchOrders");
        CursorPage<Order> orders = orderRepository.search(request);
        List<OrderSearchResponseDto> responseDtos = orders.getContent().stream().map(OrderSearchResponseDto::fromOrder).collect(Collectors.toList());
        return new CursorPage<>(responseDtos, orders.getNextCursor(), orders.isHasNext());
    }


    @Transactional
    public OrderCancelResponseDto cancelOrder(Long userId, String role, String slackId, OrderCancelServiceDto request) {

        Order order = validateOrderExists(request.getOrderId());

        if (order.getStatus() != Order.OrderStatus.PREPARING) {
            throw new OrderException.OrderCancelFailedException();
        }

        switch (role) {
            case "MASTER":
                return cancelOrderByMaster(order, userId, role, slackId);
            case "HUB":
                return cancelOrderByHubManager(order, userId, role, slackId);
            default:
                throw new OrderException.ForbiddenException();
        }

    }

    @Transactional
    public void deleteOrder(Long orderId, Long userId, String role) {
        Order order = validateOrderExists(orderId);

        switch (role) {
            case "MASTER":
                deleteOrderByMaster(order, userId);
                break;
            case "HUB":
                deleteOrderByHubManager(order, userId);
                break;
            default:
                throw new OrderException.ForbiddenException();
        }

    }

    private OrderCancelResponseDto cancelOrderByMaster(Order order, Long userId, String role, String slackId) {
        cancelOrderAndSendPaymentCancelMessage(order, userId, role, slackId);
        return OrderCancelResponseDto.fromOrder(order);
    }

    private OrderCancelResponseDto cancelOrderByHubManager(Order order, Long userId, String role, String slackId) {
        ResponseEntity<SingleResponse<StoreFindResponseDto>> supplyStore = storeService.getStore(order.getSupplyStoreId());
        if (supplyStore.getBody().getErrorCode() != null) {
            throw new OrderException.InvalidStoreException();
        }
        ResponseEntity<SingleResponse<HubFindResponseDto>> hub = hubService.getHub(supplyStore.getBody().getData().getHubId());
        if (!Objects.equals(hub.getBody().getData().getCreatedBy(), userId)) {
            throw new OrderException.ForbiddenException();
        }
        cancelOrderAndSendPaymentCancelMessage(order, userId, role, slackId);
        return OrderCancelResponseDto.fromOrder(order);
    }


    private void cancelOrderAndSendPaymentCancelMessage(Order order, Long userId, String role, String slackId) {
        order.paymentCancelRequested();
        PaymentCancelMessage paymentMessage = PaymentCancelMessage.builder().orderId(order.getId()).build();
        orderKafkaProducer.sendPaymentCancelMessage(paymentMessage, userId, role, slackId);
    }


    private void deleteOrderByMaster(Order order, Long userId) {
        bulkDeleteOrderAndOrderItems(order, userId);
    }

    private void deleteOrderByHubManager(Order order, Long userId) {
        ResponseEntity<SingleResponse<StoreFindResponseDto>> supplyStore = storeService.getStore(order.getSupplyStoreId());
        ResponseEntity<SingleResponse<HubFindResponseDto>> hub = hubService.getHub(supplyStore.getBody().getData().getHubId());
        if (!Objects.equals(hub.getBody().getData().getCreatedBy(), userId)) {
            throw new OrderException.ForbiddenException();
        }
        bulkDeleteOrderAndOrderItems(order, userId);
    }

    private void bulkDeleteOrderAndOrderItems(Order order, Long userId) {
        orderItemRepository.bulkDeleteOrderItems(order.getId(), OrderItem.OrderItemStatus.CANCELED, userId, LocalDateTime.now());
        order.delete(userId);
    }

    private Order validateOrderExists(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderException.OrderNotFoundException::new);
    }

    private Order validateOrderWithItemsExists(Long orderId) {
        return orderRepository.findByIdWithItems(orderId).orElseThrow(OrderException.OrderNotFoundException::new);
    }

    private void validateOrderItemsStatus(List<OrderItem> items) {
        items.forEach(orderItem -> {
            if (orderItem.getStatus() != OrderItem.OrderItemStatus.PREPARING) {
                throw new OrderException.OrderItemCancelFailedException();
            }
        });
    }


    public void restoreInventory(Order order) {

        ResponseEntity<Void> itemResponse = itemService.validateItems(itemRequestMapper.toItemRestoreDto(order, "INCREASE"));
        if (itemResponse.getStatusCode() == HttpStatus.BAD_REQUEST){
            throw new OrderException.RestoreInventoryFailedException();
        }
    }

    @Transactional
    public void processOrderCancellation(Long orderId, String status) {
        Order order = validateOrderWithItemsExists(orderId);
        updateOrderStatus(order, status);
        restoreInventory(order);
        log.info("restore success");
        validateOrderItemsStatus(order.getItems());
        orderItemRepository.bulkUpdateOrderItemsStatus(order.getId(), OrderItem.OrderItemStatus.CANCELED);
    }

    @Transactional
    public DeliveryRequestMessage processDeliveryRequest(Long orderId, String status) {
        DeliveryRequestMessage deliveryMessage = (DeliveryRequestMessage) redisTemplate.opsForValue().get(String.valueOf(orderId));
        if (deliveryMessage == null) {
            throw new OrderException.DeliveryNotFoundException();
        }
        redisTemplate.delete(String.valueOf(orderId));
        Order order = validateOrderExists(orderId);
        updateOrderStatus(order, status);
        return deliveryMessage;
    }

    @Transactional
    public void processDeliverySuccessRequest(Long orderId, String status) {
        Order order = validateOrderExists(orderId);
        updateOrderStatus(order, status);
    }

    @Transactional
    public PaymentCancelMessage processDeliveryFailedRequest(Long orderId, String status) {
        Order order = validateOrderWithItemsExists(orderId);
        updateOrderStatus(order, status);
        restoreInventory(order);
        return PaymentCancelMessage.builder()
                .orderId(order.getId())
                .build();
    }

    @Transactional
    public void processDeliveryUpdatedRequest(Long orderId, String status) {
        Order order = validateOrderExists(orderId);
        updateOrderStatus(order, status);
    }

    @Transactional
    public void updateOrderStatus(Order order, String status) {
        switch (status) {
            case "PAYMENT_SUCCESS":
                order.completedPayment();
                break;
            case "DELIVERING":
                order.startDelivery();
                break;
            case "DELIVERY_COMPLETED":
                order.completedDelivery();
                break;
            case "CANCELED":
                order.cancel();
                break;
            default:
                break;
        }
    }

}

