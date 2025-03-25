package com.toanyone.user.user.presentation.dto;

import com.toanyone.user.user.domain.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseUserDto {

    private Long userId;
    private String nickname;
    private String slackId;
    private UserRole role;
    private Long hubId;
    private String phone;

    public ResponseUserDto(Long userId, String nickname, String slackId, UserRole role, Long hubId, String phone
    ) {
        this.userId = userId;
        this.nickname = nickname;
        this.slackId = slackId;
        this.role = role;
        this.hubId = hubId;
        this.phone = phone;
    }
}
