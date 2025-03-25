package com.toanyone.ai.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResponseCreateSlackDto {

    private String slackId;
    private String message;
    private Long orderId;


    public static ResponseCreateSlackDto createSlackDto(String slackId, String message, Long orderId){

        ResponseCreateSlackDto dto = new ResponseCreateSlackDto();
        dto.slackId = slackId;
        dto.message = message;
        dto.orderId = orderId;

        return dto;
    }
}
