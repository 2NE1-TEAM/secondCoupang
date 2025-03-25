package com.toanyone.hub.common.config.feignConfig;

import com.toanyone.hub.infrastructure.configuration.SlackClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new SlackClientErrorDecoder();
    }
}