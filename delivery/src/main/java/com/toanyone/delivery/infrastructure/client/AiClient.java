package com.toanyone.delivery.infrastructure.client;

import com.toanyone.delivery.application.dtos.request.RequestCreateMessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service")
public interface AiClient {
    @PostMapping("/total")
    public void sendMessage(@RequestBody RequestCreateMessageDto request);
}
