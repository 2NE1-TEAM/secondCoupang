package com.toanyone.delivery.presentation;

import com.toanyone.delivery.application.DeliveryService;
import com.toanyone.delivery.application.dtos.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dtos.request.GetDeliveryManagerSearchConditionRequestDto;
import com.toanyone.delivery.application.dtos.response.CreateDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dtos.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.common.utils.MultiResponse;
import com.toanyone.delivery.common.utils.SingleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/delivery-manager/{deliveryMangerId}")
    public ResponseEntity<?> getDeliveryManager(@PathVariable("deliveryMangerId") Long deliveryManagerId) {
        GetDeliveryManagerResponseDto response = deliveryService.getDeliveryManager(deliveryManagerId);
        return ResponseEntity.ok(SingleResponse.success(response));

    }

    @GetMapping("/delivery-manager")
    public ResponseEntity<?> getDeliveryManagers(@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                                                 @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                 @RequestBody @Valid GetDeliveryManagerSearchConditionRequestDto request) {
        Page<GetDeliveryManagerResponseDto> responseDtos = deliveryService.getDeliveryManagers(page, pageSize, request);
        return ResponseEntity.ok(MultiResponse.success(responseDtos));
    }

}
