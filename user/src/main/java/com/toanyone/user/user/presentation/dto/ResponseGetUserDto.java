package com.toanyone.user.user.presentation.dto;

import com.toanyone.user.user.domain.UserRole;
import com.toanyone.user.user.domain.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@NoArgsConstructor
public class ResponseGetUserDto {

    private String nickName;
    private String slackId;
    private UserRole role;
    private Long hubId;
    private String phone;

    public ResponseGetUserDto(User user){
        nickName = user.getNickName();
        slackId = user.getSlackId();
        role = user.getRole();
        hubId = user.getHubId();
        phone = user.getPhone();
    }
}
