package com.toanyone.delivery.common.config;

import com.toanyone.delivery.common.utils.CustomHeaderInterceptor;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.beans.Encoder;

@Configuration
public class FeignConfig {

    @Bean
    public CustomHeaderInterceptor customHeaderInterceptor() {
        return new CustomHeaderInterceptor();
    }

//    @Bean
//    public Encoder feignEncoder() {
//        return new SpringEncoder(() -> new HttpMessageConverters(new MappingJackson2HttpMessageConverter()));
//    }
}