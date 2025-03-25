package com.toanyone.order.common.config;

import com.toanyone.order.common.interceptor.CustomHeaderInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public CustomHeaderInterceptor customHeaderInterceptor() {
        return new CustomHeaderInterceptor();
    }
}
