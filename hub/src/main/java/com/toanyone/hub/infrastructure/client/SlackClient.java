package com.toanyone.hub.infrastructure.client;

import com.toanyone.hub.common.config.feignConfig.FeignConfig;
import com.toanyone.hub.infrastructure.client.dto.RequestCreateMessageDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "slack-service", configuration = FeignConfig.class)
public interface SlackClient {

    @PostMapping(value = "/slacks",  consumes = "application/json")
    void sendSlackMessage(@RequestBody RequestCreateMessageDto requestCreateMessageDto);
}