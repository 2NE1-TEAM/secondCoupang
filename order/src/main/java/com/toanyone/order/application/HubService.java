package com.toanyone.order.application;

import com.toanyone.order.application.dto.HubFindResponseDto;
import com.toanyone.order.common.SingleResponse;

public interface HubService {
    SingleResponse<HubFindResponseDto> getHub(Long hubId);
}
