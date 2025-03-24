package com.toanyone.ai.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestCreateSlackDto {

    private String slackId;
    private String message;
    private Long orderId;
}
