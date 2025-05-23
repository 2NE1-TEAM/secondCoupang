package com.toanyone.delivery.presentation;

import com.toanyone.delivery.application.DeliveryService;
import com.toanyone.delivery.application.dto.request.GetDeliverySearchConditionRequestDto;
import com.toanyone.delivery.application.dto.request.UpdateDeliveryRequestDto;
import com.toanyone.delivery.application.dto.response.DeleteDeliveryResponseDto;
import com.toanyone.delivery.application.dto.response.GetDeliveryResponseDto;
import com.toanyone.delivery.application.dto.response.UpdateDeliveryResponseDto;
import com.toanyone.delivery.common.utils.MultiResponse;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.common.utils.SingleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @GetMapping("/cursor")
    public ResponseEntity<?> getDeliveries(@ModelAttribute GetDeliverySearchConditionRequestDto request) {
        CursorPage<GetDeliveryResponseDto> responseDtos = deliveryService.getDeliveries(request);
        return ResponseEntity.ok(MultiResponse.success(responseDtos));
    }

    @GetMapping("/offset")
    public ResponseEntity<?> getDeliveries(Pageable pageable, @ModelAttribute GetDeliverySearchConditionRequestDto request) {
        Page<GetDeliveryResponseDto> response = deliveryService.getDeliveries(pageable, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{deliveryId}")
    public ResponseEntity<?> updatedDelivery(@PathVariable("deliveryId") Long deliveryId, @RequestBody @Valid UpdateDeliveryRequestDto request) {
        UpdateDeliveryResponseDto response = deliveryService.updateDelivery(deliveryId, request);
        return ResponseEntity.ok(SingleResponse.success(response));
    }

    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<?> deleteDelivery(@PathVariable("deliveryId") Long deliveryId) {
        DeleteDeliveryResponseDto response = deliveryService.deleteDelivery(deliveryId);
        return ResponseEntity.ok(SingleResponse.success(response));
    }


}
