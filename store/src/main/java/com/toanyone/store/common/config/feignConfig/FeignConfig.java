package com.toanyone.store.common.config.feignConfig;

import com.toanyone.store.common.config.interceptor.UserInfoHeaderInterceptor;
import com.toanyone.store.infrastructure.configuration.HubClientErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public ErrorDecoder errorDecoder() {
        return new HubClientErrorDecoder();
    }

    @Bean
    public RequestInterceptor userInfoHeaderInterceptor(UserInfoHeaderInterceptor interceptor) {
        return interceptor;
    }
}