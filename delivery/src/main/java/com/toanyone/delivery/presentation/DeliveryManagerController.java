package com.toanyone.delivery.presentation;

import com.toanyone.delivery.application.DeliveryManagerService;
import com.toanyone.delivery.application.dto.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dto.request.GetDeliveryManagerSearchConditionRequestDto;
import com.toanyone.delivery.application.dto.request.UpdateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dto.response.CreateDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dto.response.DeleteDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dto.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dto.response.UpdateDeliveryManagerResponseDto;
import com.toanyone.delivery.common.utils.MultiResponse;
import com.toanyone.delivery.common.utils.SingleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delivery-manager")
@RequiredArgsConstructor
public class DeliveryManagerController {
    private final DeliveryManagerService deliveryManagerService;


    @PostMapping
    public ResponseEntity<?> createDeliverManager(@RequestBody @Valid CreateDeliveryManagerRequestDto request) {
        Long deliveryManagerId = deliveryManagerService.createDeliveryManager(request);
        CreateDeliveryManagerResponseDto response = CreateDeliveryManagerResponseDto.from(deliveryManagerId);
        return ResponseEntity.ok(SingleResponse.success(response));
    }

    @GetMapping("/{deliveryMangerId}")
    public ResponseEntity<?> getDeliveryManager(@PathVariable("deliveryMangerId") Long deliveryManagerId) {
        GetDeliveryManagerResponseDto response = deliveryManagerService.getDeliveryManager(deliveryManagerId);
        return ResponseEntity.ok(SingleResponse.success(response));

    }

    @GetMapping
    public ResponseEntity<?> getDeliveryManagers(@ModelAttribute GetDeliveryManagerSearchConditionRequestDto request) {
        MultiResponse.CursorPage<GetDeliveryManagerResponseDto> responseDtos = deliveryManagerService.getDeliveryManagers(request);
        return ResponseEntity.ok(MultiResponse.success(responseDtos));
    }

    @PutMapping("/{deliveryManagerId}")
    public ResponseEntity<?> updateDeliveryMangaer(@PathVariable("deliveryManagerId") Long deliveryManagerId, @RequestBody @Valid UpdateDeliveryManagerRequestDto request) {
        UpdateDeliveryManagerResponseDto response = deliveryManagerService.updateDeliveryManager(deliveryManagerId, request);
        return ResponseEntity.ok(SingleResponse.success(response));
    }


    @DeleteMapping("/{deliveryMangerId}")
    public ResponseEntity<?> deleteDeliveryManager(@PathVariable("deliveryMangerId") Long deliveryManagerId) {
        DeleteDeliveryManagerResponseDto response = deliveryManagerService.deleteDeliveryManager(deliveryManagerId);
        return ResponseEntity.ok(SingleResponse.success(response));
    }
}
