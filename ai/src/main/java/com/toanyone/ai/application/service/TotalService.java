package com.toanyone.ai.application.service;

import com.toanyone.ai.domain.entity.Ai;
import com.toanyone.ai.domain.entity.OrderStatus;
import com.toanyone.ai.presentation.dto.RequestCreateMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TotalService {

    private final SlackService slackService;
    private final AiService aiService;
    private final String ADD = "\n 정보가 다음과 같을때, 물품 발송을 최소 언제쯤 해야 할까? 구제적인 날짜와 시간을 30자 이내 존칭으로 말해줘";

    public void sendMessageToAiAndSlack(RequestCreateMessageDto requestCreateMessageDto) {

        String question = aiService.createQuestion(requestCreateMessageDto);

        String answer = aiService.createAnswer(question+ADD);

        slackService.sendMessage(question + "\n" + answer);

        Ai ai = aiService.save(question, answer);

        slackService.save(ai, question + "\n" + answer, OrderStatus.SUCCESS);
    }
}
