package com.toanyone.order.application.dto.service.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlackMessageRequestDto {
    String slackId;
    String message;
    Long orderId;
}
