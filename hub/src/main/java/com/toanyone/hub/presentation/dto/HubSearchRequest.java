package com.toanyone.hub.presentation.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자는 Spring이 필요로 해서 추가
public class HubSearchRequest {
    private String keyword;
    private String telephone;
    private Long lastHubId;
    private LocalDateTime lastCreatedAt;
    private String lastHubName;
}
