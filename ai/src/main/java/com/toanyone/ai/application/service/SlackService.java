package com.toanyone.ai.application.service;

import com.toanyone.ai.domain.entity.Ai;
import com.toanyone.ai.domain.entity.OrderStatus;
import com.toanyone.ai.domain.entity.SlackMessage;
import com.toanyone.ai.infrastructure.SlackRepository;
import com.toanyone.ai.presentation.dto.ResponseGetSlackDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor
public class SlackService {

    private final WebClient slackWebClient;
    private final SlackRepository slackRepository;

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

    public void save(Ai ai, String message, OrderStatus success) {

        SlackMessage slackMessage = SlackMessage.createSlackMessage(ai, message, success);

        slackRepository.save(slackMessage);

    }

    public ResponseEntity<Page<ResponseGetSlackDto>> getSlacks(Pageable pageable) {

        Page<ResponseGetSlackDto> dtoPage = this.slackRepository.findAllByOrderByIdDesc(pageable).map(ResponseGetSlackDto::new);

        return ResponseEntity.ok(dtoPage);
    }
}
