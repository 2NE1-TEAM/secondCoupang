package com.toanyone.store.presentation.controller;

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
    public ResponseEntity createStore(@RequestBody @Valid StoreCreateRequestDto storeCreateRequestDto) {
        log.debug("Create Store: {}", storeCreateRequestDto);
        StoreCreateResponseDto storeCreateResponseDto = storeService.createStore(storeCreateRequestDto);
        return ResponseEntity.ok(SingleResponse.success(storeCreateResponseDto));
    }

    @GetMapping("/{storeId}")
    public ResponseEntity findStore(@PathVariable Long storeId) {
        log.debug("Find Store: {}", storeId);
        StoreFindResponseDto storeFindResponseDto = storeService.findOne(storeId);
        return ResponseEntity.ok(SingleResponse.success(storeFindResponseDto));
    }

    @GetMapping
    public ResponseEntity findAllStore(
            @ModelAttribute StoreSearchRequest searchRequest,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        CursorPage<StoreFindResponseDto> stores = storeService.findStores(searchRequest, sortBy, direction, size);
        return ResponseEntity.ok(MultiResponse.success(stores));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity deleteStore(@PathVariable Long storeId) {
        log.debug("Delete Store: {}", storeId);
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{storeId}")
    public ResponseEntity<Void> updateStore(@PathVariable Long storeId,
                                            @RequestBody StoreUpdateRequestDto requestDto) {
        log.debug("Update Store: {}", storeId);
        storeService.updateStore(storeId, requestDto);
        return ResponseEntity.noContent().build();
    }

}
