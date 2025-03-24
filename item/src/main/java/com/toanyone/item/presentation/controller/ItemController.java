package com.toanyone.item.presentation.controller;

import com.toanyone.item.common.config.annotation.RequireRole;
import com.toanyone.item.domain.service.ItemService;
import com.toanyone.item.presentation.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @RequireRole({"MASTER", "HUB", "STORE"})
    @PostMapping
    public ResponseEntity createItem(@RequestBody @Valid ItemCreateRequestDto itemCreateRequestDto) {
        ItemCreateResponseDto itemCreateResponseDto = itemService.createItem(itemCreateRequestDto);
        return ResponseEntity.ok(SingleResponse.success(itemCreateResponseDto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity findItem(@PathVariable Long itemId) {
        ItemFindResponseDto itemFindResponseDto = itemService.findOne(itemId);
        return ResponseEntity.ok(SingleResponse.success(itemFindResponseDto));
    }

    @GetMapping
    public ResponseEntity findAllItems(
            @ModelAttribute ItemSearchRequest searchRequest,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        CursorPage<ItemFindResponseDto> items = itemService.findItems(searchRequest, sortBy, direction, size);
        return ResponseEntity.ok(MultiResponse.success(items));
    }

    @RequireRole({"MASTER", "HUB"})
    @DeleteMapping("/{itemId}")
    public ResponseEntity deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole({"MASTER", "HUB", "STORE"})
    @PatchMapping("/{itemId}")
    public ResponseEntity updateItem(@PathVariable Long itemId,
                                            @RequestBody @Valid ItemUpdateRequestDto requestDto) {
        return ResponseEntity.ok(SingleResponse.success(itemService.updateItem(itemId, requestDto)));
    }

    @PatchMapping("/adjust-stock")
    public ResponseEntity adjustMultipleStocks(@RequestBody @Valid List<ItemStockRequestDto> requestDtos) {
        itemService.adjustStock(requestDtos);
        return ResponseEntity.ok().build();
    }
}
