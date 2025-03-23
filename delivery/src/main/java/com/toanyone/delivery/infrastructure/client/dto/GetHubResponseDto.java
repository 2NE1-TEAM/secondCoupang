package com.toanyone.delivery.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GetHubResponseDto {
    @JsonProperty("hub_id")
    private Long hubId;

    public static GetHubResponseDto from(Long hubId) {
        GetHubResponseDto response = new GetHubResponseDto();
        response.hubId = hubId;
        return response;
    }
}
