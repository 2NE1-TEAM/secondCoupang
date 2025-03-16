package com.toanyone.order.presentation;

import com.toanyone.order.application.OrderService;
import com.toanyone.order.common.SingleResponse;
import com.toanyone.order.presentation.dto.request.OrderCancelRequestDto;
import com.toanyone.order.presentation.dto.request.OrderCreateRequestDto;
import com.toanyone.order.presentation.dto.response.OrderCancelResponseDto;
import com.toanyone.order.presentation.dto.response.OrderCreateResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
