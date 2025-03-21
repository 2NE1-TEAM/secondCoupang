package com.toanyone.hub.application.service;

import com.toanyone.hub.domain.exception.HubException;
import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.model.HubDistance;
import com.toanyone.hub.domain.repository.HubRepository;
import com.toanyone.hub.domain.service.HubService;
import com.toanyone.hub.infrastructure.messaging.KafkaProducerService;
import com.toanyone.hub.presentation.dto.*;
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
public class HubServiceImpl implements HubService {

    private final HubRepository hubRepository;
    private final KafkaProducerService kafkaProducerService;
    private final CacheManager cacheManager;

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

        // 허브간 거리 테이블에 데이터 추가를 비동기 처리를 위한 카프카 프로듀서 로직
        kafkaProducerService.createHub(savedHub);

        // 허브 생성 시 단건 조회 허브에 저장을 위해 단건 조회 시 사용하는 DTO 생성 후 cacheManager 활용.
        HubFindResponseDto hubDto = new HubFindResponseDto(
                savedHub.getId(),
                savedHub.getHubName(),
                savedHub.getAddress(),
                savedHub.getLocation(),
                savedHub.getTelephone()
        );

        Objects.requireNonNull(cacheManager.getCache("hubFindOne")).put(savedHub.getId(), hubDto);
        return new HubCreateResponseDto(savedHub.getId());
    }

    @Override
    @Cacheable(value = "hubFindOne", key = "args[0]") // 들어온 파라미터 첫번째를 key로 설정.
    public HubFindResponseDto findOne(Long hubId) {
        log.info("HubServiceImpl :: findOne :: hubId:{}", hubId);
        Hub findHub = hubRepository.findById(hubId).orElseThrow(() -> new HubException.HubNotFoundException("허브를 찾을 수 없습니다."));
        return new HubFindResponseDto(findHub.getId(), findHub.getHubName(), findHub.getAddress(), findHub.getLocation(), findHub.getTelephone());
    }

    @Override
    @Cacheable(
            value = "hubSearchCache",
            key = "(#searchRequest.keyword != null ? #searchRequest.keyword : 'ALL')"
                    + " + '-' + #sortBy + '-' + #direction"
                    + " + '-' + (#searchRequest.lastHubId != null ? #searchRequest.lastHubId : 0)"
                    + " + '-' + (#searchRequest.lastCreatedAt != null ? #searchRequest.lastCreatedAt.toString() : '0000-00-00T00:00:00')"
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
        Hub findHub = hubRepository.findFetchById(hubId).orElseThrow(() -> new HubException.HubNotFoundException("허브를 찾을 수 없습니다."));
        //허브간 거리 삭제
        if(!findHub.getHubDistances().isEmpty()) {
            for (HubDistance hubDistance : findHub.getHubDistances()) {
                hubDistance.delete();
            }
        }
        // 허브 삭제
        findHub.delete();
    }
}
