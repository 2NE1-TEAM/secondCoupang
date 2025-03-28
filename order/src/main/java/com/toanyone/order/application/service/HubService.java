package com.toanyone.order.application.service;

import com.toanyone.order.application.dto.service.response.HubFindResponseDto;
import com.toanyone.order.common.dto.SingleResponse;
import org.springframework.http.ResponseEntity;

public interface HubService {
    ResponseEntity<SingleResponse<HubFindResponseDto>> getHub(Long hubId);
}
