package com.toanyone.hub.presentation.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class RouteDTO {
    @JsonProperty("routes")
    private Route[] routes;

    @Data
    public static class Route {
        private Summary summary;
    }

    @Data
    public static class Summary {
        private long distance;  // 거리
        private long duration;  // 시간
    }
}