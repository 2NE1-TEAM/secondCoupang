package com.toanyone.ai.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder) {

        return webClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient slackWebClient(WebClient.Builder webClientBuilder) {

        return webClientBuilder
                .baseUrl("https://hooks.slack.com/services/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")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
