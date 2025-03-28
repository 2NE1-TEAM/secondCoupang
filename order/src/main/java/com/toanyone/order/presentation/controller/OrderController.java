package com.toanyone.order.presentation.controller;

import com.toanyone.order.application.service.OrderService;
import com.toanyone.order.application.dto.service.request.OrderCancelServiceDto;
import com.toanyone.order.application.dto.service.request.OrderCreateServiceDto;
import com.toanyone.order.application.dto.service.request.OrderFindAllCondition;
import com.toanyone.order.application.dto.service.request.OrderSearchCondition;
import com.toanyone.order.common.dto.CursorPage;
import com.toanyone.order.common.dto.MultiResponse;
import com.toanyone.order.common.dto.SingleResponse;
import com.toanyone.order.common.config.UserContext;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.request.OrderFindAllRequestDto;
import com.toanyone.order.presentation.dto.request.OrderSearchRequestDto;
import com.toanyone.order.presentation.dto.response.*;
import com.toanyone.order.presentation.mapper.OrderApplicationToServiceDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j(topic = "OrderController")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderApplicationToServiceDtoMapper orderApplicationToServiceDtoMapper;

    @PostMapping
    public ResponseEntity<SingleResponse<OrderCreateResponseDto>> createOrder(@RequestBody @Valid OrderCreateRequestDto request) {
        OrderCreateServiceDto serviceDto = orderApplicationToServiceDtoMapper.toOrderCreateServiceDto(request);
        UserContext userContext = UserContext.getUserContext();
        return ResponseEntity.status(HttpStatus.CREATED).body(SingleResponse.success(orderService.createOrder(userContext.getUserId(),userContext.getRole(), userContext.getSlackId(), serviceDto)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<SingleResponse<OrderFindResponseDto>> findOrder(@PathVariable("orderId") Long orderId) {
        log.info("findOrder");
        return ResponseEntity.ok().body(SingleResponse.success(orderService.findOrder(orderId)));
    }

    @GetMapping
    public ResponseEntity<MultiResponse<OrderFindAllResponseDto>> findOrders(
            @ModelAttribute OrderFindAllRequestDto request
    ) {
        log.info("findOrders");
        UserContext userContext = UserContext.getUserContext();
        OrderFindAllCondition condition = orderApplicationToServiceDtoMapper.toOrderFindAllCondition(request);
        CursorPage<OrderFindAllResponseDto> response = orderService.findOrders(userContext.getUserId(), condition);

        return ResponseEntity.ok().body(MultiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<MultiResponse<OrderSearchResponseDto>> searchOrders(
            @ModelAttribute OrderSearchRequestDto request
    ) {

        log.info("searchOrders: keyword={}, userId={}, hubId={}, cursorId={}, timestamp={}, sortType={}, size={}",
                request.getKeyword(), request.getUserId(), request.getHubId(), request.getNextCursorOrderId(),
                request.getTimestamp(), request.getSortType(), request.getSize());
        OrderSearchCondition condition = orderApplicationToServiceDtoMapper.toOrderSearchCondition(request);
        CursorPage<OrderSearchResponseDto> response = orderService.searchOrders(condition);

        return ResponseEntity.ok().body(MultiResponse.success(response));
    }


    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<SingleResponse<OrderCancelResponseDto>> cancelOrder(
            @PathVariable("orderId") Long orderId) {
        OrderCancelServiceDto serviceDto = orderApplicationToServiceDtoMapper.toOrderCancelServiceDto(orderId);
        UserContext userContext = UserContext.getUserContext();
        return ResponseEntity.ok().body(SingleResponse.success(orderService.cancelOrder(userContext.getUserId(), userContext.getRole(), userContext.getSlackId(), serviceDto)));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity deleteOrder(@PathVariable("orderId") Long orderId) {
        UserContext userContext = UserContext.getUserContext();
        orderService.deleteOrder(orderId, userContext.getUserId(),userContext.getRole());
        return ResponseEntity.noContent().build();
    }


}
