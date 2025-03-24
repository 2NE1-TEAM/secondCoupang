package com.toanyone.ai.application.service;

import com.toanyone.ai.domain.entity.Ai;
import com.toanyone.ai.infrastructure.AiRepository;
import com.toanyone.ai.presentation.dto.RequestCreateMessageDto;
import com.toanyone.ai.presentation.dto.RequestGeminiDto;
import com.toanyone.ai.presentation.dto.ResponseGeminiDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient webClient;
    private final AiRepository aiRepository;

    @Value("${ai.gemini.key}")
    private String apiKey;

    public String createAnswer(String question) {
        RequestGeminiDto request = new RequestGeminiDto(
                List.of(new RequestGeminiDto.Content(
                        List.of(new RequestGeminiDto.Part(question))))
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

        return response.getCandidates()
                .get(0)
                .getContent()
                .getParts()
                .get(0)
                .getText();
    }

    public String createQuestion(RequestCreateMessageDto r) {

        return "주문 번호 : " + r.getOrderId() + "\n" +
                "주문자 정보 : " + r.getOrderNickName() + " / " + r.getOrderSlackId() + "\n" +
                "상품 정보 : " + r.getItemInfo() + "\n" +
                "요청 사항 : " + r.getRequest() + "\n" +
                "발송지 : " + r.getShippingAddress() + "\n" +
                "경유지 : " + r.getStopOver() + "\n" +
                "도착지 : " + r.getDestination() + "\n" +
                "배송담당자 : " + r.getDeliveryPerson() + " / " + r.getDeliveryPersonSlackId() + "\n";

    }

    public Ai save(String question, String answer, HttpServletRequest request) {

        Ai ai = Ai.createAi(question, answer);
        aiRepository.save(ai);
        ai.updateCreated(Long.parseLong(request.getHeader("X-User-Id")));
        ai.updateUpdated(Long.parseLong(request.getHeader("X-User-Id")));

        return ai;
    }
}
