package com.toanyone.ai.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${slack.api}")
    private String slackApi;

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
                .baseUrl("https://hooks.slack.com/services/"+slackApi)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
