package com.toanyone.delivery.infrastructure.client.dto;

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

}
