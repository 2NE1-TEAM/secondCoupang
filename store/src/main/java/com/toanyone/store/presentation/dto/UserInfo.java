package com.toanyone.store.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfo {
    private Long userId;
    private String role;
    private String slackId;
}