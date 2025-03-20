package com.toanyone.user.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestLoginUserDto {

    @NotNull @Email
    private String slackId;

    @NotNull
    private String password;
}
