package com.toanyone.delivery.presentation;

import com.toanyone.delivery.application.DeliveryService;
import com.toanyone.delivery.application.dtos.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dtos.request.GetDeliveryManagerSearchConditionRequestDto;
import com.toanyone.delivery.application.dtos.request.GetDeliverySearchConditionRequestDto;
import com.toanyone.delivery.application.dtos.request.UpdateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dtos.response.*;
import com.toanyone.delivery.common.utils.MultiResponse;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.common.utils.SingleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @GetMapping("/{deliveryId}")
    public ResponseEntity<?> getDelivery(@PathVariable("deliveryId") Long deliveryId) {
        GetDeliveryResponseDto response = deliveryService.getDelivery(deliveryId);
        return ResponseEntity.ok(SingleResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<?> getDeliveries(@ModelAttribute GetDeliverySearchConditionRequestDto request) {
        CursorPage<GetDeliveryResponseDto> responseDtos = deliveryService.getDeliveries(request);
        return ResponseEntity.ok(MultiResponse.success(responseDtos));
    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<?> deleteDelivery(@PathVariable("deliveryId") Long deliveryId) {
        DeleteDeliveryResponseDto response = deliveryService.deleteDelivery(deliveryId);
        return ResponseEntity.ok(SingleResponse.success(response));
    }

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
    public ResponseEntity<?> getDeliveryManagers(@ModelAttribute GetDeliveryManagerSearchConditionRequestDto request) {
        CursorPage<GetDeliveryManagerResponseDto> responseDtos = deliveryService.getDeliveryManagers(request);
        return ResponseEntity.ok(MultiResponse.success(responseDtos));
    }

    @PutMapping("/delivery-manager/{deliveryManagerId}")
    public ResponseEntity<?> updateDeliveryMangaer(@PathVariable("deliveryManagerId") Long deliveryManagerId, @RequestBody @Valid UpdateDeliveryManagerRequestDto request) {
        UpdateDeliveryManagerResponseDto response = deliveryService.updateDeliveryManager(deliveryManagerId, request);
        return ResponseEntity.ok(SingleResponse.success(response));
    }


    @DeleteMapping("/delivery-manager/{deliveryMangerId}")
    public ResponseEntity<?> deleteDeliveryManager(@PathVariable("deliveryMangerId") Long deliveryManagerId) {
        DeleteDeliveryManagerResponseDto response = deliveryService.deleteDeliveryManager(deliveryManagerId);
        return ResponseEntity.ok(SingleResponse.success(response));
    }

}
