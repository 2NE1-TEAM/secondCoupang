package com.toanyone.delivery.common.config;

import com.toanyone.delivery.common.utils.CustomHeaderInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public CustomHeaderInterceptor customHeaderInterceptor() {
        return new CustomHeaderInterceptor();
    }
}