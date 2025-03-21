package com.toanyone.hub.presentation.dto;

import com.toanyone.hub.domain.model.Hub;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HubDto implements Serializable {
    private Long id;
    private String hubName;
    private LocationDto location;

    public static HubDto fromEntity(Hub hub) {
        return new HubDto(
                hub.getId(),
                hub.getHubName(),
                new LocationDto(hub.getLocation().getLatitude(), hub.getLocation().getLongitude())
        );
    }
}