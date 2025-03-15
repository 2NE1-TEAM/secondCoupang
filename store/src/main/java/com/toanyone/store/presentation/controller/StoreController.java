package com.toanyone.store.presentation.controller;

import com.toanyone.store.domain.service.StoreService;
import com.toanyone.store.presentation.dto.SingleResponse;
import com.toanyone.store.presentation.dto.StoreCreateRequestDto;
import com.toanyone.store.presentation.dto.StoreCreateResponseDto;
import com.toanyone.store.presentation.dto.StoreFindResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

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

    @DeleteMapping("/{storeId}")
    public ResponseEntity deleteStore(@PathVariable Long storeId) {
        log.debug("Delete Store: {}", storeId);
        storeService.deleteStore(storeId);
        return ResponseEntity.noContent().build();
    }

}
