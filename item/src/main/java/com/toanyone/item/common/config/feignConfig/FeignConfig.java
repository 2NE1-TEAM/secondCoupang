package com.toanyone.item.common.config.feignConfig;

import com.toanyone.item.infrastructure.configuration.StoreClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new StoreClientErrorDecoder();
    }
}