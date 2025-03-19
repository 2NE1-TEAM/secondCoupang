package com.toanyone.user.user.presentation.dto;

import com.toanyone.user.user.domain.UserRole;
import com.toanyone.user.user.domain.entity.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseEditUserDto {

    private Long userId;
    private String nickName;
    private String slackId;
    private UserRole role;
    private String phone;



    public static ResponseEditUserDto createResponseEditUserDto(User user) {
        ResponseEditUserDto responseEditUserDto = new ResponseEditUserDto();
        responseEditUserDto.userId = user.getId();
        responseEditUserDto.nickName = user.getNickName();
        responseEditUserDto.slackId = user.getSlackId();
        responseEditUserDto.role = user.getRole();
        responseEditUserDto.phone = user.getPhone();

        return responseEditUserDto;
    }
}
