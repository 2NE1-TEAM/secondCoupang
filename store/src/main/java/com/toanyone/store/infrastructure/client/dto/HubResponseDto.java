package com.toanyone.store.infrastructure.client.dto;

import com.toanyone.store.domain.model.DetailAddress;
import com.toanyone.store.domain.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HubResponseDto {
    private Long hubId;
    private String hubName;
    private DetailAddress detailAddress;
    private Location location;
}
