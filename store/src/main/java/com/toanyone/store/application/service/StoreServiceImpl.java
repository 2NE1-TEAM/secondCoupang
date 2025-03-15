package com.toanyone.store.application.service;

import com.toanyone.store.common.util.PhoneNumberUtils;
import com.toanyone.store.domain.exception.StoreException;
import com.toanyone.store.domain.model.Store;
import com.toanyone.store.domain.repository.StoreRepository;
import com.toanyone.store.domain.service.StoreService;
import com.toanyone.store.infrastructure.client.HubClient;
import com.toanyone.store.infrastructure.client.dto.HubResponseDto;
import com.toanyone.store.presentation.dto.SingleResponse;
import com.toanyone.store.presentation.dto.StoreCreateRequestDto;
import com.toanyone.store.presentation.dto.StoreCreateResponseDto;
import com.toanyone.store.presentation.dto.StoreFindResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final HubClient hubClient;

    @Override
    @Transactional
    public StoreCreateResponseDto createStore(StoreCreateRequestDto storeCreateRequestDto) {
        validateHubExists(storeCreateRequestDto.getHubId()); // 등록한 허브가 존재하는 허브인지 확인
        validateStoreNameNotExists(storeCreateRequestDto); // 등록할 업체 이름 중복 확인

        Store store = Store.create(
                storeCreateRequestDto.getStoreName(),
                storeCreateRequestDto.getHubId(),
                storeCreateRequestDto.getDetailAddress(),
                storeCreateRequestDto.getLocation(),
                storeCreateRequestDto.getStoreType(),
                PhoneNumberUtils.normalizePhoneNumber(storeCreateRequestDto.getTelephone()) // 전화번호에서 "-" 제거
        );

        storeRepository.save(store);
        return new StoreCreateResponseDto(store.getId());
    }

    @Override
    public StoreFindResponseDto findOne(Long storeId) {
        Store findStore = validateStoreExists(storeId);
        HubResponseDto hubResponseDto = validateHubExists(findStore.getHubId());
        return StoreFindResponseDto.of(findStore, hubResponseDto.getHubId(), hubResponseDto.getHubName());

    }

    @Override
    @Transactional
    public void deleteStore(Long storeId) {
        Store findStore = validateStoreExists(storeId); // 존재하는 업체인지 검증
        findStore.delete(findStore.getId());
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
        SingleResponse<HubResponseDto> response = hubClient.getHubById(hubId);
        if (response.getData() == null) throw new StoreException.HubNotFoundException("존재하지 않는 허브입니다.");
        return response.getData();
    }

    /**
     * 존재하는 업체인지 검증
     */
    private Store validateStoreExists(Long storeId) {
        return storeRepository.findById(storeId).orElseThrow(() -> new StoreException.StoreNotFoundException("해당 업체를 찾을 수 없습니다."));
    }
}