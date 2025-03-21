package com.toanyone.order.application;

import com.toanyone.order.application.dto.request.OrderCancelServiceDto;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.application.dto.request.OrderFindAllCondition;
import com.toanyone.order.application.dto.request.OrderSearchCondition;
import com.toanyone.order.application.mapper.ItemRequestMapper;
import com.toanyone.order.common.CursorPage;
import com.toanyone.order.common.exception.OrderException;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.entity.OrderItem;
import com.toanyone.order.domain.repository.OrderItemRepository;
import com.toanyone.order.domain.repository.OrderRepository;
import com.toanyone.order.presentation.dto.request.OrderCancelRequestDto;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "OrderService")
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemService itemService;
    private final StoreService storeService;
    private final ItemRequestMapper itemRequestMapper;

    @Transactional
    public OrderCreateResponseDto createOrder(OrderCreateServiceDto request) {

        boolean isValidSupplyStore = storeService.validateStore(request.getSupplyStoreId());
        boolean isValidReceiveStore = storeService.validateStore(request.getReceiveStoreId());

        if (!isValidSupplyStore || !isValidReceiveStore) {
            throw new OrderException.InvalidStoreException();
        }

        //Todo: 주문 처리가 완료되지 않은 상태에서 같은 입력의 주문 예외 처리

        //Item 검증
        boolean isValid = itemService.validateItems(itemRequestMapper.toItemValidationRequestDto(request));

        if (!isValid) {
            throw new OrderException.InsufficientStockException();
        }

        Order order = Order.create(request.getUserId(), request.getSupplyStoreId(), request.getReceiveStoreId());

        log.info("orderId : {}", order.getId());

        request.getItems().stream().map(validRequest ->
                        OrderItem.create(validRequest.getItemId(),
                                validRequest.getItemName(),
                                validRequest.getQuantity(),
                                validRequest.getPrice()))
                .forEach(order::addOrderItem);

        order.calculateTotalPrice();

        orderRepository.save(order);

        log.info("orderId: {}, userId: {}, totalPrice: {}", order.getId(), order.getUserId(), order.getTotalPrice());

        //Todo: Payment 작업 추가

        //Todo: Delivery 관련 작업 추가

        return OrderCreateResponseDto.fromOrder(order);
    }


    @Transactional
    public OrderCancelResponseDto cancelOrder(OrderCancelServiceDto request) {

        Order order = validateOrderWithItemsExists(request.getOrderId());

        try {

            //ItemClient에 재고 restore
            boolean restoreSuccess = itemService.restoreInventory(itemRequestMapper.toItemRestoreDto(order));
            if (!restoreSuccess) {
                throw new OrderException.RestoreInventoryFailedException();
            }

            //Todo: 결제 취소
            //Todo: 배송 취소 메시지

            validateOrderItemsStatus(order.getItems());

            orderItemRepository.bulkUpdateOrderItemsStatus(order.getId(), OrderItem.OrderItemStatus.CANCELED);

            order.cancel();

            return OrderCancelResponseDto.fromOrder(order);

        } catch (Exception e) {
            throw new OrderException.OrderCancelFailedException();
        }

    }

    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        Order order = validateOrderExists(orderId);
        orderItemRepository.bulkDeleteOrderItems(order.getId(), OrderItem.OrderItemStatus.CANCELED, userId, LocalDateTime.now()); //상태 상관 없이 관리자가 삭제 강제
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


    private void validateOrderAlreadyExists(Long orderId) {
        orderRepository.findById(orderId).ifPresent( order -> {
            throw new OrderException.OrderNotFoundException();
        });
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

}

