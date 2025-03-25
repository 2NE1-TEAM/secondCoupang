package com.toanyone.user.user.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestDeleteUserDto {

    @NotNull
    private Long userId;
}
