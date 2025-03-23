package com.toanyone.store.presentation.dto;

import com.toanyone.store.common.util.PhoneNumberUtils;
import com.toanyone.store.domain.model.DetailAddress;
import com.toanyone.store.domain.model.Location;
import com.toanyone.store.domain.model.StoreType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreUpdateRequestDto {
    @Size(max = 30)
    private String storeName;
    private StoreType storeType;
    private Location location;
    private DetailAddress detailAddress;
    private Long hubId;
    private String telephone;

    public String formatingTelephone(@NotBlank String telephone) {
        this.telephone = PhoneNumberUtils.normalizePhoneNumber(telephone);
        return this.telephone;
    }
}
