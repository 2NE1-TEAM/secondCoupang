package com.toanyone.ai.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class SlackService {

    private final WebClient slackWebClient;

    public void sendMessage(String message) {
        String payload = "{\"text\":\"" + message + "\"}";

        slackWebClient
                .post()
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> System.out.println("Message sent successfully"),
                        error -> System.err.println("Error sending message: " + error.getMessage())
                );
    }
}
