package com.toanyone.delivery.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryManagerRequestDto {
    @JsonProperty("delivery_manager_type")
    @NotNull
    private String deliveryManagerType;
    @JsonProperty("hub_id")
    @NotNull
    private Long hubId;
    @JsonProperty("user_id")
    @NotNull
    private Long userId;
    @NotNull
    private String name;
}
