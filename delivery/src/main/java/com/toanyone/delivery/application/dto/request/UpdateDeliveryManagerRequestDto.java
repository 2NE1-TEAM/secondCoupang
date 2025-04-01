package com.toanyone.delivery.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateDeliveryManagerRequestDto {
    @NotNull
    private String name;

}
