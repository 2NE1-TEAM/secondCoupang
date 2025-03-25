package com.toanyone.store.presentation.controller;

import com.toanyone.store.common.config.annotation.RequireRole;
import com.toanyone.store.domain.service.StoreService;
import com.toanyone.store.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/stores")
public class StoreController {
    private final StoreService storeService;

    @PostMapping
    @RequireRole({"MASTER", "HUB"})
    public ResponseEntity createStore(@RequestBody @Valid StoreCreateRequestDto storeCreateRequestDto) {
        log.debug("StoreController Create Store: {}", storeCreateRequestDto);
        StoreCreateResponseDto storeCreateResponseDto = storeService.createStore(storeCreateRequestDto);
        return ResponseEntity.ok(SingleResponse.success(storeCreateResponseDto));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity findStore(@PathVariable Long storeId) {
        log.debug("StoreController Find Store: {}", storeId);
        StoreFindResponseDto storeFindResponseDto = storeService.findOne(storeId);
        return ResponseEntity.ok(SingleResponse.success(storeFindResponseDto));
    }

    @GetMapping
    public ResponseEntity findAllStore(
            @ModelAttribute StoreSearchRequest searchRequest,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        log.debug("StoreController Find All Store: {}", searchRequest);
        CursorPage<StoreFindResponseDto> stores = storeService.findStores(searchRequest, sortBy, direction, size);
        return ResponseEntity.ok(MultiResponse.success(stores));
    }

    @RequireRole({"MASTER", "HUB"})
    @DeleteMapping("/{storeId}")
    public ResponseEntity deleteStore(@PathVariable Long storeId) {
        log.debug("StoreController Delete Store: {}", storeId);
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole({"MASTER", "HUB", "STORE"})
    @PatchMapping("/{storeId}")
    public ResponseEntity updateStore(@PathVariable Long storeId,
                                            @RequestBody @Valid StoreUpdateRequestDto requestDto) {
        log.debug("StoreController Update Store: {}, requestDto: {}", storeId, requestDto);
        return ResponseEntity.ok(SingleResponse.success(storeService.updateStore(storeId, requestDto)));
    }
}
