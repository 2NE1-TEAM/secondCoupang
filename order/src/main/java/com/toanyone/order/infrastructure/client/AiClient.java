package com.toanyone.order.infrastructure.client;

import com.toanyone.order.application.service.AiService;
import com.toanyone.order.application.dto.SlackMessageRequestDto;
import com.toanyone.order.common.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service", configuration = FeignConfig.class)
public interface AiClient extends AiService {

    @PostMapping("/slack")
    void sendSlackMessage(@RequestBody SlackMessageRequestDto requestDto);

}
