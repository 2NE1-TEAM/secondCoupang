package com.toanyone.hub.common.config.feignConfig;

import com.toanyone.hub.common.config.interceptor.UserInfoHeaderInterceptor;
import com.toanyone.hub.common.filter.UserContext;
import com.toanyone.hub.infrastructure.configuration.SlackClientErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor userInfoHeaderInterceptor(UserContext userContext) {
        return new UserInfoHeaderInterceptor(userContext);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new SlackClientErrorDecoder();
    }
}