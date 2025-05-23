package com.toanyone.hub.domain.service;

import com.toanyone.hub.presentation.dto.*;
import jakarta.validation.Valid;

public interface HubService {
    CursorPage<HubFindResponseDto> findHubs(HubSearchRequest searchRequest, String sortBy, String direction, int size);

    HubCreateResponseDto createHub(HubCreateRequestDto hubCreateRequestDto);

    HubFindResponseDto findOne(Long hubId);

    void deleteHub(Long hubId, Long userId);

    HubUpdateResponseDto updateHub(Long hubId, HubUpdateRequestDto requestDto);
}
