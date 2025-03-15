package com.toanyone.delivery.presentation;

import com.toanyone.delivery.application.DeliveryService;
import com.toanyone.delivery.application.dtos.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dtos.response.CreateDeliveryManagerResponseDto;
import com.toanyone.delivery.common.SingleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PostMapping("/delivery-manager")
    public ResponseEntity<?> createDeliverManager(@RequestBody @Valid CreateDeliveryManagerRequestDto request) {
        Long deliveryManagerId = deliveryService.createDeliveryManager(request);
        CreateDeliveryManagerResponseDto response = CreateDeliveryManagerResponseDto.from(deliveryManagerId);
        return ResponseEntity.ok(SingleResponse.success(response));
    }

}
