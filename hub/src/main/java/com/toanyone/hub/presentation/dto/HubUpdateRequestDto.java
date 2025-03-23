package com.toanyone.hub.presentation.dto;

import com.toanyone.hub.common.util.PhoneNumberUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HubUpdateRequestDto {
    private Long userId;
    private String hubName;
    private String telephone;

    public String formatingTelephone(@NotBlank String telephone) {
        this.telephone = PhoneNumberUtils.normalizePhoneNumber(telephone);
        return this.telephone;
    }
}
