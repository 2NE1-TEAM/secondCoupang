package com.toanyone.store.application.service;

import com.toanyone.store.common.filter.UserContext;
import com.toanyone.store.common.util.PhoneNumberUtils;
import com.toanyone.store.domain.exception.StoreException;
import com.toanyone.store.domain.model.Store;
import com.toanyone.store.domain.repository.StoreRepository;
import com.toanyone.store.domain.service.StoreService;
import com.toanyone.store.infrastructure.client.HubClient;
import com.toanyone.store.infrastructure.client.dto.HubResponseDto;
import com.toanyone.store.presentation.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final HubClient hubClient;
    private final CacheManager cacheManager;
    private final UserContext userContext;

    /**
     * 스토어 신규 생성
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "hubSearchCache", allEntries = true)
    public StoreCreateResponseDto createStore(StoreCreateRequestDto storeCreateRequestDto) {
        log.info("StoreServiceImpl :: createStore :: storeCreateRequestDto :{}", storeCreateRequestDto);
        HubResponseDto hubResponseDto = validateHubExists(storeCreateRequestDto.getHubId());// 등록한 허브가 존재하는 허브인지 확인
        validateStoreFieldNotExists(storeCreateRequestDto); // 등록할 업체 각 필드 중복 체크

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

        // 생성과 동시에 단건 조회 캐시에 넣음
        StoreFindResponseDto storeFindResponseDto = StoreFindResponseDto.of(store, store.getId(), store.getHubName());
        Objects.requireNonNull(cacheManager.getCache("storeCache")).put(store.getId(), storeFindResponseDto);
        return new StoreCreateResponseDto(store.getId());
    }

    /**
     * 스토어 단건 조회
     */
    @Override
    @Cacheable(cacheNames = "storeCache", key = "args[0]")
    public StoreFindResponseDto findOne(Long storeId) {
        log.info("StoreServiceImpl :: findOne :: storeId :{}", storeId);
        Store findStore = validateStoreExists(storeId);
        validateHubExists(findStore.getHubId());
        return StoreFindResponseDto.of(findStore, findStore.getHubId(), findStore.getHubName());
    }

    /**
     * 스토어 복수 조회 및 검색
     */
    @Override
    @Cacheable(
            value = "hubSearchCache",
            key = "T(org.springframework.util.StringUtils).hasText(#searchRequest?.keyword) ? #searchRequest.keyword : 'ALL'"
                    + " + '-' + #sortBy + '-' + #direction"
                    + " + '-' + (#searchRequest?.lastStoreId != null ? #searchRequest.lastStoreId : 0)"
                    + " + '-' + (#searchRequest?.lastCreatedAt != null ? #searchRequest.lastCreatedAt.format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMMddHHmmss')) : '00000000000000')"
    )
    public CursorPage findStores(StoreSearchRequest searchRequest, String sortBy, String direction, int size) {
        log.info("StoreServiceImpl :: findStores :: searchRequest :{}", searchRequest);
        if (searchRequest.getHubId() != null) { // 존재하는 허브인지 확인
            validateHubExists(searchRequest.getHubId());
        }

        return storeRepository.search(searchRequest, sortBy, direction, size);
    }

    /**
     * 스토어 삭제
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "storeCache", key = "#storeId"),
            @CacheEvict(cacheNames = "hubSearchCache", allEntries = true)
    })
    public void deleteStore(Long storeId) {
        log.info("StoreServiceImpl :: deleteStore :: storeId :{}", storeId);
        Store findStore = validateStoreExists(storeId); // 존재하는 업체인지 검증
        validateHubCheck(findStore.getDeletedBy()); // 허브매니저인지 체크
        findStore.delete(userContext.getUser().getUserId());
    }

    /**
     * 스토어 업데이트
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "hubSearchCache", allEntries = true)
    public StoreUpdateResponseDto updateStore(Long storeId, StoreUpdateRequestDto requestDto) {
        log.info("StoreServiceImpl :: updateStore :: storeId :{} :: requestDto :{}", storeId, requestDto);

        Store findStore = validateStoreExists(storeId);

        Long createdBy = hubClient.getHubById(findStore.getHubId()).getBody().getData().getCreatedBy();
        validateHubCheck(createdBy); // 해당 업체가 소속된 허브의 매니저인지
        validateStoreCheck(createdBy); // 해당 업체의 담당자인지

        validateStoreFieldNotExistsForUpdate(storeId, requestDto);
        applyUpdates(requestDto, findStore);

        // 업데이트 후 스토어 단건 조회 캐시에 넣음.
        StoreFindResponseDto storeFindResponseDto = StoreFindResponseDto.of(findStore, storeId, requestDto.getStoreName());
        Objects.requireNonNull(cacheManager.getCache("storeCache")).put(storeId, storeFindResponseDto);
        return new StoreUpdateResponseDto(findStore.getId());
    }

    /**
     * 등록하려는 업체 중복 체크
     */
    private void validateStoreFieldNotExists(StoreCreateRequestDto dto) {
        if (storeRepository.existsByStoreName(dto.getStoreName())) {
            throw new StoreException.StoreDuplicateException("존재하는 업체명입니다.");
        }
        if (storeRepository.existsByLocation(dto.getLocation())) {
            throw new StoreException.StoreDuplicateException("동일한 좌표의 업체가 존재합니다.");
        }
        if (storeRepository.existsByTelephone(PhoneNumberUtils.normalizePhoneNumber(dto.getTelephone()))) {
            throw new StoreException.StoreDuplicateException("동일한 전화번호의 업체가 존재합니다.");
        }
    }

    /**
     * 수정하려는 업체 중복 체크
     */
    private void validateStoreFieldNotExistsForUpdate(Long storeId, StoreUpdateRequestDto dto) {
        if (dto.getStoreName() != null && storeRepository.existsByStoreNameAndIdNot(dto.getStoreName(), storeId)) {
            throw new StoreException.StoreDuplicateException("이미 존재하는 업체명입니다.");
        }
        if (dto.getLocation() != null && storeRepository.existsByLocationAndIdNot(dto.getLocation(), storeId)) {
            throw new StoreException.StoreDuplicateException("동일한 좌표의 업체가 존재합니다.");
        }
        if (dto.getTelephone() != null && storeRepository.existsByTelephoneAndIdNot(dto.formatingTelephone(dto.getTelephone()), storeId)) {
            throw new StoreException.StoreDuplicateException("동일한 전화번호의 업체가 존재합니다.");
        }
    }

    /**
     * 존재하는 허브인지 검증
     */
    private HubResponseDto validateHubExists(Long hubId) {
        return Objects.requireNonNull(hubClient.getHubById(hubId).getBody()).getData();
    }

    /**
     * 존재하는 업체인지 검증
     */
    private Store validateStoreExists(Long storeId) {
        return storeRepository.findById(storeId).orElseThrow(() -> new StoreException.StoreNotFoundException("해당 업체를 찾을 수 없습니다."));
    }

    /**
     * 업데이드 할 때 null 체크
     */
    private void applyUpdates(StoreUpdateRequestDto dto, Store store) {
        if (dto.getStoreName() != null) {
            store.updateStoreName(dto.getStoreName());
        }
        if (dto.getStoreType() != null) {
            store.updateStoreType(dto.getStoreType());
        }
        if (dto.getLocation() != null) {
            store.updateLocation(dto.getLocation());
        }
        if (dto.getDetailAddress() != null) {
            store.updateDetailAddress(dto.getDetailAddress());
        }
        if (dto.getTelephone() != null) {
            store.updateTelephone(dto.getTelephone());
        }
        if (dto.getHubId() != null) {
            HubResponseDto hub = validateHubExists(dto.getHubId());
            store.updateHub(dto.getHubId(), hub.getHubName());
        }
    }

    private void validateStoreCheck(Long createdBy) {
        if(userContext.getUser().getRole().equals("STORE")) {
            if(!createdBy.equals(userContext.getUser().getUserId())){
                throw new StoreException.StoreDeniedException("해당 압체의 담당자가 아닙니다.");
            }
        }
    }

    private void validateHubCheck(Long createdBy) {
        if(userContext.getUser().getRole().equals("HUB")) {
            if(!createdBy.equals(userContext.getUser().getUserId())){
                throw new StoreException.StoreDeniedException("해당 업체가 소속 된 허브 매니저가 아닙니다.");
            }
        }
    }
}