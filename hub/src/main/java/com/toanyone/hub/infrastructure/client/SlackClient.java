package com.toanyone.hub.infrastructure.client;

import com.toanyone.hub.common.config.feignConfig.NoHeaderFeignConfig;
import com.toanyone.hub.infrastructure.client.dto.RequestCreateMessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "ai-service"
)
public interface SlackClient {
    @PostMapping(value = "/slack", consumes = "application/json")
    void sendSlackMessage(
            @RequestHeader("X-User-Roles") String role,
            @RequestHeader("X-Slack-Id") String slackId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody RequestCreateMessageDto requestCreateMessageDto
    );
}