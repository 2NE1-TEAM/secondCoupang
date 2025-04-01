package com.toanyone.delivery.infrastructure.client;

import com.toanyone.delivery.application.dto.request.RequestCreateMessageDto;
import com.toanyone.delivery.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service", configuration = FeignConfig.class)
public interface AiClient {
    @PostMapping("/total")
    public void sendMessage(@RequestBody RequestCreateMessageDto request);
}
