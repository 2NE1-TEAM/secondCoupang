package com.toanyone.hub.infrastructure.repository;

import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.presentation.dto.CursorPage;
import com.toanyone.hub.presentation.dto.HubFindResponseDto;
import com.toanyone.hub.presentation.dto.HubSearchRequest;

public interface HubRepositoryCustom {
    CursorPage<HubFindResponseDto> search(HubSearchRequest hubSearchRequest, String sortBy, String direction, int size);
}
