package com.toanyone.ai.application.service;

import com.toanyone.ai.presentation.dto.RequestGeminiDto;
import com.toanyone.ai.presentation.dto.ResponseGeminiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient webClient;
    private final SlackService slackService;

    @Value("${ai.gemini.key}")
    private String apiKey;

    public String generateContent(String text) {
        RequestGeminiDto request = new RequestGeminiDto(
                List.of(new RequestGeminiDto.Content(
                        List.of(new RequestGeminiDto.Part(text))))
        );

        ResponseGeminiDto response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/gemini-2.0-flash:generateContent")
                        .queryParam("key", apiKey)
                        .build())
                .bodyValue(request) // 요청 본문 설정
                .retrieve()
                .bodyToMono(ResponseGeminiDto.class)
                .block(); // 응답을 String으로 변환

        String answer = response.getCandidates()
                .get(0)
                .getContent()
                .getParts()
                .get(0)
                .getText();

        slackService.sendMessage(answer);

        return answer;
    }
}
