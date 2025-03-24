package com.toanyone.hub.application.service;

import com.toanyone.hub.common.filter.UserContext;
import com.toanyone.hub.domain.exception.HubException;
import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.model.HubDistance;
import com.toanyone.hub.domain.repository.HubRepository;
import com.toanyone.hub.domain.service.HubService;
import com.toanyone.hub.infrastructure.messaging.KafkaProducerService;
import com.toanyone.hub.infrastructure.messaging.dto.HubCreateMessage;
import com.toanyone.hub.presentation.dto.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class HubServiceImpl implements HubService {

    private final HubRepository hubRepository;
    private final KafkaProducerService kafkaProducerService;
    private final CacheManager cacheManager;
    private final UserContext userContext;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "routeCache", allEntries = true),
            @CacheEvict(cacheNames = "hubSearchCache", allEntries = true)
    })
    public HubCreateResponseDto createHub(HubCreateRequestDto hubCreateRequestDto) {
        log.info("HubServiceImpl :: createHub :: hubCreateRequestDto:{}", hubCreateRequestDto);

        if (hubRepository.existsByHubName(hubCreateRequestDto.getHubName())) {
            throw new HubException.HubDuplicateException("동일한 허브 이름이 존재합니다.");
        } else if (hubRepository.existsByAddress(hubCreateRequestDto.getAddress())) {
            throw new HubException.HubDuplicateException("동일한 주소의 허브가 존재합니다.");
        } else if (hubRepository.existsByLocation(hubCreateRequestDto.getLocation())) {
            throw new HubException.HubDuplicateException("동일한 좌표의 허브가 존재합니다.");
        } else if (hubRepository.existsByTelephone(hubCreateRequestDto.getTelephone())) {
            throw new HubException.HubDuplicateException("동일한 전화번호의 허브가 존재합니다.");
        }

        //  새 허브 저장
        Hub savedHub = hubRepository.save(
                Hub.createHub(hubCreateRequestDto.getHubName(),
                        hubCreateRequestDto.getLocation(),
                        hubCreateRequestDto.getAddress(),
                        hubCreateRequestDto.getTelephone()));

//        kafkaProducerService.createHub(new HubCreateMessage(savedHub.getId(), userContext.getUser().getSlackId()));
        eventPublisher.publishEvent(new HubCreateMessage(savedHub.getId(), userContext.getUser().getSlackId(), userContext.getUser().getUserId(), userContext.getUser().getRole()));


        // 허브 생성 시 단건 조회 허브에 저장을 위해 단건 조회 시 사용하는 DTO 생성 후 cacheManager 활용.
        HubFindResponseDto hubDto = new HubFindResponseDto(
                savedHub.getId(),
                savedHub.getHubName(),
                savedHub.getAddress(),
                savedHub.getLocation(),
                savedHub.getTelephone(),
                savedHub.getCreatedBy()
        );

        Objects.requireNonNull(cacheManager.getCache("hubFindOne")).put(savedHub.getId(), hubDto);
        return new HubCreateResponseDto(savedHub.getId());
    }

    @Override
    @Cacheable(value = "hubFindOne", key = "args[0]") // 들어온 파라미터 첫번째를 key로 설정.
    public HubFindResponseDto findOne(Long hubId) {
        log.info("HubServiceImpl :: findOne :: hubId:{}", hubId);
        Hub findHub = validateExistHub(hubId); // 존재하는 허브인지 체크
        return new HubFindResponseDto(findHub.getId(), findHub.getHubName(), findHub.getAddress(), findHub.getLocation(), findHub.getTelephone(), findHub.getCreatedBy());
    }

    @Override
    @Cacheable(
            value = "hubSearchCache",
            key = "T(org.springframework.util.StringUtils).hasText(#searchRequest?.keyword) ? #searchRequest.keyword : 'ALL'"
                    + " + '-' + #sortBy + '-' + #direction"
                    + " + '-' + (#searchRequest?.lastHubId != null ? #searchRequest.lastHubId : 0)"
                    + " + '-' + (#searchRequest?.lastCreatedAt != null ? #searchRequest.lastCreatedAt.format(T(java.time.format.DateTimeFormatter).ofPattern('yyyyMMddHHmmss')) : '00000000000000')"
    )
    public CursorPage<HubFindResponseDto> findHubs(HubSearchRequest searchRequest, String sortBy, String direction, int size) {
        log.info("HubServiceImpl :: findHubs :: searchRequest:{}", searchRequest);
        return hubRepository.search(searchRequest, sortBy, direction, size);
    }

    @Override
    @Transactional
    // 허브 삭제 시 허브간 거리 캐시 삭제, 허브 단건 조회에서 삭제, 허브 복수 조회 캐시 삭제
    @Caching(evict = {
            @CacheEvict(cacheNames = "routeCache", allEntries = true),
            @CacheEvict(cacheNames = "hubFindOne", key = "#hubId"),
            @CacheEvict(cacheNames = "hubSearchCache", allEntries = true)
    })
    public void deleteHub(Long hubId) {
        log.info("HubServiceImpl :: deleteHub :: hubId:{}", hubId);
        Hub findHub = validateExistHub(hubId); // 존재하는 허브인지 체크

        //허브간 거리 삭제
        if(!findHub.getHubDistances().isEmpty()) {
            for (HubDistance hubDistance : findHub.getHubDistances()) {
                hubDistance.delete(userContext.getUser().getUserId());
            }
        }
        // 허브 삭제
        findHub.delete(userContext.getUser().getUserId());
    }

    @Override
    @Transactional
    public HubUpdateResponseDto updateHub(Long hubId, HubUpdateRequestDto requestDto) {
        log.info("HubServiceImpl :: updateHub :: hubId:{} :: requestDto: {}", hubId, requestDto);
        Hub findHub = validateExistHub(hubId);
        validateHubFieldNotExistsForUpdate(hubId, requestDto);
        applyUpdates(requestDto, findHub);
        return new HubUpdateResponseDto(findHub.getId());
    }

    private Hub validateExistHub(Long hubId) {
        return hubRepository.findById(hubId).orElseThrow(() -> new HubException.HubNotFoundException("허브를 찾을 수 없습니다."));
    }

    private void validateHubFieldNotExistsForUpdate(Long storeId, HubUpdateRequestDto dto) {
        if (dto.getHubName() != null && hubRepository.existsByHubNameAndIdNot(dto.getHubName(), storeId)) {
            throw new HubException.HubDuplicateException("이미 존재하는 허브명입니다.");
        }
        if (dto.getTelephone() != null && hubRepository.existsByTelephoneAndIdNot(dto.formatingTelephone(dto.getTelephone()), storeId)) {
            throw new HubException.HubDuplicateException("동일한 전화번호의 허브가 존재합니다.");
        }
    }

    /**
     * 업데이드 할 때 null 체크
     */
    private void applyUpdates(HubUpdateRequestDto dto, Hub hub) {
        if (dto.getHubName() != null) {
            hub.updateStoreName(dto.getHubName());
        }

        if (dto.getTelephone() != null) {
            hub.updateTelephone(dto.getTelephone());
        }

        // 허브매니저 변경
        if (dto.getUserId() != null) {
            hub.updateCreatedBy(dto.getUserId());
        }
    }
}
