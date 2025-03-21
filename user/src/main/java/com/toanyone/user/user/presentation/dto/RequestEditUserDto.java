package com.toanyone.user.user.presentation.dto;

import com.toanyone.user.user.domain.UserRole;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestEditUserDto {

    @NotNull
    Long userId;

    @NotNull
    private String nickName;

    @NotNull
    private UserRole role;

    @NotNull
    private String phone;

    private String password;

    private String newPassword;
}
