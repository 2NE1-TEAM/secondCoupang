package com.toanyone.order.application;

import com.toanyone.order.application.dto.HubFindResponseDto;
import com.toanyone.order.common.SingleResponse;
import org.springframework.http.ResponseEntity;

public interface HubService {
    ResponseEntity<SingleResponse<HubFindResponseDto>> getHub(Long hubId);
}
