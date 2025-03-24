package com.toanyone.hub.presentation.dto;

import com.toanyone.hub.domain.model.Address;
import com.toanyone.hub.domain.model.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class HubFindResponseDto implements Serializable {
    private Long hubId;
    private String hubName;
    private Address address;
    private Location location;
    private String telephone;
    private Long createdBy;
    // 허브에 소속된 배달기사, 허브매니저를 보여줄 지?
}
