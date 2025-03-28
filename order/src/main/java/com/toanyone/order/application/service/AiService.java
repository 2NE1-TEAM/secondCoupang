package com.toanyone.order.application.service;

import com.toanyone.order.application.dto.service.request.SlackMessageRequestDto;

public interface AiService {

    void sendSlackMessage(SlackMessageRequestDto requestDto);

}
