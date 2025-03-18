package com.toanyone.user.user.presentation.dto;

import com.toanyone.user.user.domain.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseUserDto {

    private Long userId;
    private String nickname;
    private String password;
    private String slackId;
    private UserRole role;
    private Long hubId;

    public ResponseUserDto(Long userId, String nickname, String password, String slackId, UserRole role, Long hubId
    ) {
        this.userId = userId;
        this.nickname = nickname;
        this.password = password;
        this.slackId = slackId;
        this.role = role;
        this.hubId = hubId;
    }
}
