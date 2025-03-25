package com.toanyone.hub.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@ToString
public class HubCreateMessage implements Serializable {
    private Long hubId;
    private String slackId;
    private Long userId;
    private String role;
}
