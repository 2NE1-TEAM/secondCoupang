package com.toanyone.user.user.presentation.dto;

import com.toanyone.user.user.domain.UserRole;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@ToString
public class RequestCreateUserDto {

    @NotNull
//    @Size(min = 4, max = 10, message = "아이디는 4자 이상, 10자 이하여야 합니다.")
//    @Pattern(regexp = "^[a-z0-9]+$", message = "아이디는 알파벳 소문자(a-z)와 숫자(0-9)만 포함해야 합니다.")
    private String nickName;

    @NotNull
//    @Size(min = 8, max = 15, message = "비밀번호는 8자 이상, 15자 이하여야 합니다.")
//    @Pattern(
//            regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z0-9!@#$%^&*]+$",
//            message = "비밀번호는 알파벳 대소문자(a-z, A-Z), 숫자(0-9), 특수문자(!@#$%^&*)를 포함해야 합니다."
//    )
    private String password;

    @NotNull @Email
    private String slackId;
    @NotNull
    private UserRole role;
    @NotNull
    private Long hubId;
    @NotNull
    private String phone;
}
