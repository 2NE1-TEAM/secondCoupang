package com.toanyone.order.application.service;

import com.toanyone.order.application.dto.SlackMessageRequestDto;
import org.springframework.web.bind.annotation.RequestBody;

public interface AiService {

    void sendSlackMessage(SlackMessageRequestDto requestDto);

}
