package com.toanyone.ai.presentation.dto;

import com.toanyone.ai.domain.entity.OrderStatus;
import com.toanyone.ai.domain.entity.SlackMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResponseGetSlackDto {

    private Long id;
    private String message;
    private OrderStatus orderStatus;

    public ResponseGetSlackDto(SlackMessage slackMessage){
        id = slackMessage.getId();
        message = slackMessage.getMessage();
        orderStatus = slackMessage.getOrderStatus();
    }
}
