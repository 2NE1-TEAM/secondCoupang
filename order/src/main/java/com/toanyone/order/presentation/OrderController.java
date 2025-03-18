package com.toanyone.order.presentation;

import com.toanyone.order.application.OrderService;
import com.toanyone.order.common.CursorPage;
import com.toanyone.order.common.MultiResponse;
import com.toanyone.order.common.SingleResponse;
import com.toanyone.order.presentation.dto.request.OrderCancelRequestDto;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.request.OrderSearchRequestDto;
import com.toanyone.order.presentation.dto.response.OrderCancelResponseDto;
import com.toanyone.order.presentation.dto.response.OrderCreateResponseDto;
import com.toanyone.order.presentation.dto.response.OrderFindResponseDto;
import com.toanyone.order.presentation.dto.response.OrderSearchResponseDto;
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

    @PostMapping
    public ResponseEntity<SingleResponse<OrderCreateResponseDto>> createOrder(@RequestBody @Valid OrderCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(SingleResponse.success(orderService.createOrder(request)));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<SingleResponse<OrderCancelResponseDto>> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody @Valid OrderCancelRequestDto request) {
        return ResponseEntity.ok().body(SingleResponse.success(orderService.cancelOrder(orderId, request)));
    }

    //Todo: 임시로 header에서 userId 받음
    @DeleteMapping("/{orderId}")
    public ResponseEntity deleteOrder(@PathVariable Long orderId,
                                      @RequestHeader(value = "userId", required = true) Long userId) {

        orderService.deleteOrder(orderId, userId);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/{orderId}")
    public ResponseEntity<SingleResponse<OrderFindResponseDto>> findOrder(@PathVariable Long orderId) {
        log.info("findOrder");
        return ResponseEntity.ok().body(SingleResponse.success(orderService.findOrder(orderId)));
    }

    @GetMapping("/search")
    public ResponseEntity<MultiResponse<OrderSearchResponseDto>> searchOrders(
            @ModelAttribute OrderSearchRequestDto request,
            @RequestParam(defaultValue = "10") int size) {

        log.info("searchOrders: keyword={}, userId={}, hubId={}, cursorId={}, timestamp={}, sortType={}, size={}",
                request.getKeyword(), request.getUserId(), request.getHubId(), request.getCursorId(),
                request.getTimestamp(), request.getSortType(), size);

        CursorPage<OrderSearchResponseDto> response = orderService.searchOrders(request, size);

        return ResponseEntity.ok().body(MultiResponse.success(response));
    }

}
