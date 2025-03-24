package com.toanyone.item.application.service;

import com.toanyone.item.common.filter.UserContext;
import com.toanyone.item.domain.exception.ItemException;
import com.toanyone.item.domain.model.Item;
import com.toanyone.item.domain.repository.ItemRepository;
import com.toanyone.item.domain.service.ItemService;
import com.toanyone.item.infrastructure.client.StoreClient;
import com.toanyone.item.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserContext userContext;
    private final CacheManager cacheManager;
    private final StoreClient storeClient;

    /**
     * 아이템 신규 생성
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "ItemSearchCache", allEntries = true)
    public ItemCreateResponseDto createItem(ItemCreateRequestDto itemCreateRequestDto) {
        log.info("ItemServiceImpl :: createItem :: itemCreateRequestDto :{}", itemCreateRequestDto);
        validateStoreExists(itemCreateRequestDto.getStoreId());
        Item item = Item.create(itemCreateRequestDto.getItemName(), itemCreateRequestDto.getPrice(),
                itemCreateRequestDto.getStock(), itemCreateRequestDto.getImageUrl(), itemCreateRequestDto.getStoreId());

        itemRepository.save(item);

        // 생성과 동시에 단건 조회 캐시에 넣음
        ItemFindResponseDto itemFindResponseDto = ItemFindResponseDto.of(item);
        Objects.requireNonNull(cacheManager.getCache("itemCache")).put(item.getId(), itemFindResponseDto);
        return new ItemCreateResponseDto(item.getId());
    }

    /**
     * 아이템 단건 조회
     */
    @Override
    @Cacheable(cacheNames = "itemCache", key = "args[0]")
    public ItemFindResponseDto findOne(Long itemId) {
        Item findItem = validateExistItem(itemId);
        return ItemFindResponseDto.of(findItem);
    }

    /**
     * 아이템 복수 조회 및 검색
     */
    @Override
    @Cacheable(
            value = "itemSearchCache",
            key = "T(org.springframework.util.StringUtils).hasText(#searchRequest?.keyword) ? #searchRequest.keyword : 'ALL'"
                    + " + '-' + #sortBy + '-' + #direction"
                    + " + '-' + (#searchRequest?.lastItemId != null ? #searchRequest.lastItemId : 0)"
                    + " + '-' + (#searchRequest?.lastCreatedAt != null ? #searchRequest.lastCreatedAt.format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMMddHHmmss')) : '00000000000000')"
                    + " + '-' + (#searchRequest?.lastItemPrice != null ? #searchRequest.lastItemPrice : 0)"
    )
    public CursorPage findItems(ItemSearchRequest searchRequest, String sortBy, String direction, int size) {
        return itemRepository.search(searchRequest, sortBy, direction, size);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "ItemSearchCache", allEntries = true)
    public void adjustStock(List<ItemStockRequestDto> requestDtos) {
        for (ItemStockRequestDto requestDto : requestDtos) {
            Long itemId = requestDto.getItemId();
            Integer quantity = requestDto.getQuantity();
            AdjustmentType type = requestDto.getType();

            if (itemId == null || quantity == null || quantity <= 0 || type == null) {
                throw new ItemException.StockBadRequestException("요청 정보가 잘못되었습니다.");
            }

            Item item = validateExistItem(itemId);

            if (type == AdjustmentType.DECREASE) {
                if (item.getStock() >= quantity) {
                    item.reduceStock(quantity);
                } else {
                    throw new ItemException.StockReduceException("재고가 부족합니다. (itemId: " + itemId + ")");
                }
            } else if (type == AdjustmentType.INCREASE) {
                item.addStock(quantity);
            }

            // 캐시 갱신
            ItemFindResponseDto itemFindResponseDto = ItemFindResponseDto.of(item);
            Objects.requireNonNull(cacheManager.getCache("itemCache")).put(itemId, itemFindResponseDto);
        }
    }

    /**
     * 가게 삭제
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "itemCache", key = "#itemId"),
            @CacheEvict(cacheNames = "itemSearchCache", allEntries = true)
    })
    public void deleteItem(Long itemId) {
        Item findItem = validateExistItem(itemId);
        findItem.delete(userContext.getUser().getUserId());
    }

    /**
     * 스토어 업데이트
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "itemSearchCache", allEntries = true)
    public ItemUpdateResponseDto updateItem(Long itemId, ItemUpdateRequestDto requestDto) {
        Item findItem = validateExistItem(itemId);
        applyUpdates(requestDto, findItem);

        // 업데이트 후 아이템 단건 조회 캐시에 넣음.
        ItemFindResponseDto itemFindResponseDto = ItemFindResponseDto.of(findItem);
        Objects.requireNonNull(cacheManager.getCache("itemCache")).put(itemId, itemFindResponseDto);
        return new ItemUpdateResponseDto(findItem.getId());
    }


    /**
     * 업데이드 할 때 null 체크
     */
    private void applyUpdates(ItemUpdateRequestDto dto, Item item) {
        if (dto.getItemName() != null) {
            item.updateItemName(dto.getItemName());
        }

        if (dto.getPrice() != null) {
            item.updatePrice(dto.getPrice());
        }

        if (dto.getStock() != null) {
            item.updateStock(dto.getStock());
        }

        if (dto.getImageUrl()!= null) {
            item.updateImageUrl(dto.getImageUrl());
        }
    }

    /**
     * 존재하는 아이템인지 확인
     */
    private Item validateExistItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() -> new ItemException.ItemNotFoundException("존재하지 않는 상품입니다."));
    }

    /**
     * 존재하는 업체인지 검증
     */
    private void validateStoreExists(Long storeId) {
        storeClient.existStore(storeId);
    }
}