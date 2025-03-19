package com.toanyone.order.presentation;

import com.toanyone.order.application.OrderService;
import com.toanyone.order.application.dto.request.OrderCancelServiceDto;
import com.toanyone.order.application.dto.request.OrderCreateServiceDto;
import com.toanyone.order.application.dto.request.OrderFindAllCondition;
import com.toanyone.order.application.dto.request.OrderSearchCondition;
import com.toanyone.order.common.CursorPage;
import com.toanyone.order.common.MultiResponse;
import com.toanyone.order.common.SingleResponse;
import com.toanyone.order.common.UserContext;
import com.toanyone.order.presentation.dto.request.OrderCancelRequestDto;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.request.OrderFindAllRequestDto;
import com.toanyone.order.presentation.dto.request.OrderSearchRequestDto;
import com.toanyone.order.presentation.dto.response.*;
import com.toanyone.order.presentation.mapper.OrderMapper;
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
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<SingleResponse<OrderCreateResponseDto>> createOrder(@RequestBody @Valid OrderCreateRequestDto request) {
        OrderCreateServiceDto serviceDto = orderMapper.toOrderCreateServiceDto(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SingleResponse.success(orderService.createOrder(serviceDto)));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<SingleResponse<OrderCancelResponseDto>> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody @Valid OrderCancelRequestDto request) {
        OrderCancelServiceDto serviceDto = orderMapper.toOrderCancelServiceDto(orderId, request);
        return ResponseEntity.ok().body(SingleResponse.success(orderService.cancelOrder(serviceDto)));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity deleteOrder(@PathVariable Long orderId) {
        UserContext userContext = UserContext.getUserContext();
        orderService.deleteOrder(orderId, userContext.getUserId());
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/{orderId}")
    public ResponseEntity<SingleResponse<OrderFindResponseDto>> findOrder(@PathVariable Long orderId) {
        log.info("findOrder");
        return ResponseEntity.ok().body(SingleResponse.success(orderService.findOrder(orderId)));
    }

    @GetMapping
    public ResponseEntity<MultiResponse<OrderFindAllResponseDto>> findOrders(
            @ModelAttribute OrderFindAllRequestDto request
    ) {
        log.info("findOrders");
        UserContext userContext = UserContext.getUserContext();
        OrderFindAllCondition condition = orderMapper.toOrderFindAllCondition(request);
        CursorPage<OrderFindAllResponseDto> response = orderService.findOrders(userContext.getUserId(), condition);

        return ResponseEntity.ok().body(MultiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<MultiResponse<OrderSearchResponseDto>> searchOrders(
            @ModelAttribute OrderSearchRequestDto request
    ) {

        log.info("searchOrders: keyword={}, userId={}, hubId={}, cursorId={}, timestamp={}, sortType={}, size={}",
                request.getKeyword(), request.getUserId(), request.getHubId(), request.getCursorId(),
                request.getTimestamp(), request.getSortType(), request.getSize());
        OrderSearchCondition condition = orderMapper.toOrderSearchCondition(request);
        CursorPage<OrderSearchResponseDto> response = orderService.searchOrders(condition);

        return ResponseEntity.ok().body(MultiResponse.success(response));
    }

}
