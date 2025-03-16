package com.toanyone.order.application;

import com.toanyone.order.application.dto.ItemRestoreRequestDto;
import com.toanyone.order.application.dto.ItemValidationRequestDto;
import com.toanyone.order.application.mapper.ItemRequestMapper;
import com.toanyone.order.common.exception.OrderException;
import com.toanyone.order.domain.entity.Order;
import com.toanyone.order.domain.entity.OrderItem;
import com.toanyone.order.domain.repository.OrderRepository;
import com.toanyone.order.presentation.dto.request.OrderCancelRequestDto;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.response.OrderCancelResponseDto;
import com.toanyone.order.presentation.dto.response.OrderCreateResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemService itemService;
    private final ItemRequestMapper itemRequestMapper;

    @Transactional
    public OrderCreateResponseDto createOrder(OrderCreateRequestDto request) {

        //Todo: Store 검증 관련 작업 추가

        Order order = Order.create(request.getUserId(), request.getSupplyStoreId(), request.getReceiveStoreId());

        //Todo: 주문 처리가 완료되지 않은 상태에서 같은 입력의 주문 예외 처리

        //Item 검증
        boolean isValid = itemService.validateItems(itemRequestMapper.toItemValidationRequestDto(request));


        if (!isValid) {
            throw new OrderException.InsufficientStockException();
        }

        request.getItems().stream().map(validRequest ->
                        OrderItem.create(validRequest.getItemId(), validRequest.getItemName(), validRequest.getQuantity(), validRequest.getPrice()))
                .forEach(order::addOrderItem);

        order.calculateTotalPrice();

        //Todo: Payment 작업 추가

        log.debug("orderId: {}, userId: {}, totalPrice: {}", order.getId(), order.getUserId(), order.getTotalPrice());

        orderRepository.save(order);

        //Todo: Delivery 관련 작업 추가

        return OrderCreateResponseDto.fromOrder(order);
    }


    @Transactional
    public OrderCancelResponseDto cancelOrder(OrderCancelRequestDto request) {

        Order order = validateOrderExists(request.getOrderId());

        try {
            order.cancel();

            //ItemClient에 재고 restore
            boolean restoreSuccess = itemService.restoreInventory(itemRequestMapper.toItemRestoreDto(order));

            if (!restoreSuccess) {
                throw new OrderException.RestoreInventoryFailedException();
            }

            //Todo: 결제 취소
            //Todo: 배송 취소 메시지

            return OrderCancelResponseDto.fromOrder(order);

        } catch (Exception e) {
            throw new OrderException.OrderCancelFailedException();
        }

    }

    private Order validateOrderExists(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderException.OrderNotFoundException::new);
    }

    private void validateOrderAlreadyExists(Long orderId) {
        orderRepository.findById(orderId).ifPresent( order -> {
            throw new OrderException.OrderNotFoundException();
        });
    }


}
