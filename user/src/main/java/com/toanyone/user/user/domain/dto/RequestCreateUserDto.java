package com.toanyone.user.user.domain.dto;

import com.toanyone.user.user.domain.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class RequestCreateUserDto {

    private String nickName;
    private String password;
    private String slackId;
    private UserRole role;
}
