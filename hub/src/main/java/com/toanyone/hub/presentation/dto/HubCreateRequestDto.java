package com.toanyone.hub.presentation.dto;

import com.toanyone.hub.domain.model.Address;
import com.toanyone.hub.domain.model.Location;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HubCreateRequestDto {
    @NotEmpty
    private String hubName;
    @NotNull
    private Location location;
    @NotNull
    private Address address;
    @NotEmpty
    private String telephone;
}
