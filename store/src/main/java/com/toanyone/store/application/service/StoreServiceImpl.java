package com.toanyone.store.application.service;

import com.toanyone.store.common.util.PhoneNumberUtils;
import com.toanyone.store.domain.exception.StoreException;
import com.toanyone.store.domain.model.Store;
import com.toanyone.store.domain.repository.StoreRepository;
import com.toanyone.store.domain.service.StoreService;
import com.toanyone.store.infrastructure.client.HubClient;
import com.toanyone.store.infrastructure.client.dto.HubResponseDto;
import com.toanyone.store.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.FeatureDescriptor;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final HubClient hubClient;

    @Override
    @Transactional
//    @CachePut(cacheNames = "storeCache", key = "#result.storeId")
    public StoreCreateResponseDto createStore(StoreCreateRequestDto storeCreateRequestDto) {
        HubResponseDto hubResponseDto = validateHubExists(storeCreateRequestDto.getHubId());// 등록한 허브가 존재하는 허브인지 확인
        validateStoreNameNotExists(storeCreateRequestDto); // 등록할 업체 이름 중복 확인

        Store store = Store.create(
                storeCreateRequestDto.getStoreName(),
                storeCreateRequestDto.getHubId(),
                storeCreateRequestDto.getDetailAddress(),
                storeCreateRequestDto.getLocation(),
                storeCreateRequestDto.getStoreType(),
                PhoneNumberUtils.normalizePhoneNumber(storeCreateRequestDto.getTelephone()), // 전화번호에서 "-" 제거
                hubResponseDto.getHubName()
        );

        storeRepository.save(store);
        return new StoreCreateResponseDto(store.getId());
    }

    @Override
//    @Cacheable(cacheNames = "storeCache", key = "args[0]")
    public StoreFindResponseDto findOne(Long storeId) {
        Store findStore = validateStoreExists(storeId);
        validateHubExists(findStore.getHubId());
        return StoreFindResponseDto.of(findStore, findStore.getHubId(), findStore.getHubName());
    }

    @Override
    public CursorPage findStores(StoreSearchRequest storeSearchRequest, String sortBy, String direction, int size) {
        if (storeSearchRequest.getHubId() != null) { // 존재하는 허브인지 확인
            validateHubExists(storeSearchRequest.getHubId());
        }

        CursorPage<Store> stores = storeRepository.search(storeSearchRequest, sortBy, direction, size);
        List<StoreFindResponseDto> storeDtos = stores.getContent().stream().map(store -> StoreFindResponseDto.of(store, store.getHubId(), store.getHubName()))
                .toList();

       return new CursorPage<>(storeDtos, stores.getNextCursor(), stores.isHasNext());
    }

    @Override
    @Transactional
    public void deleteStore(Long storeId) {
        Store findStore = validateStoreExists(storeId); // 존재하는 업체인지 검증
        findStore.delete();
    }

    @Override
    @Transactional
//    @CachePut(cacheNames = "storeCache", key = "args[0]")
    public void updateStore(Long storeId, StoreUpdateRequestDto requestDto) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스토어를 찾을 수 없습니다."));

        copyNonNullProperties(requestDto, store);
    }

    private void copyNonNullProperties(StoreUpdateRequestDto source, Store target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        return Arrays.stream(src.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> src.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

    /**
     * 등록하려는 업체명이 존재하는 지 검증
     */
    private void validateStoreNameNotExists(StoreCreateRequestDto storeCreateRequestDto) {
        if (storeRepository.findByStoreName(storeCreateRequestDto.getStoreName()).isPresent())
            throw new StoreException.StoreNameExistException("존재하는 업체명입니다.");
    }

    /**
     * 존재하는 허브인지 검증
     */
    private HubResponseDto validateHubExists(Long hubId) {
        try {
            ResponseEntity<SingleResponse<HubResponseDto>> response = hubClient.getHubById(hubId);
            SingleResponse<HubResponseDto> responseBody = response.getBody();
            if (response.getStatusCode() != HttpStatus.OK || responseBody == null || responseBody.getData() == null) {
                throw new StoreException.HubNotFoundException("존재하지 않는 허브입니다.");
            }

            return responseBody.getData();
        } catch (Exception e) {
            throw new RuntimeException(e); // 임시
        }
    }

    /**
     * 존재하는 업체인지 검증
     */
    private Store validateStoreExists(Long storeId) {
        return storeRepository.findById(storeId).orElseThrow(() -> new StoreException.StoreNotFoundException("해당 업체를 찾을 수 없습니다."));
    }
}