package com.toanyone.delivery.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.toanyone.delivery.domain.DeliveryRoad;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class GetDeliveryRoadResponseDto {

    @JsonProperty("delivery_road_id")
    private Long deliveryRoadId;

    @JsonProperty("delivery_id")
    private Long deliveryId;

    @JsonProperty("delivery_manager_id")
    private Long deliveryManagerId;

    @JsonProperty("sequence")
    private int sequence;

    @JsonProperty("departure_hub_id")
    private Long departureHubId;

    @JsonProperty("arrival_hub_id")
    private Long arrivalHubId;

    @JsonProperty("estimated_distance")
    private BigDecimal estimatedDistance;

    @JsonProperty("estimated_duration")
    private int estimatedDuration;

    @JsonProperty("actual_distance")
    private BigDecimal actualDistance;

    @JsonProperty("actual_duration")
    private int actualDuration;

    @JsonProperty("current_status")
    private DeliveryRoad.CurrentStatus currentStatus;

    public static GetDeliveryRoadResponseDto from(DeliveryRoad deliveryRoad) {
        GetDeliveryRoadResponseDto dto = new GetDeliveryRoadResponseDto();
        dto.deliveryRoadId = deliveryRoad.getId();
        dto.deliveryId = deliveryRoad.getDelivery().getId();
        dto.deliveryManagerId = deliveryRoad.getDeliveryManagerId();
        dto.sequence = deliveryRoad.getSequence();
        dto.departureHubId = deliveryRoad.getDepartureHubId();
        dto.arrivalHubId = deliveryRoad.getArrivalHubId();
        dto.estimatedDistance = deliveryRoad.getEstimatedDistance();
        dto.estimatedDuration = deliveryRoad.getEstimatedDuration();
        dto.actualDistance = deliveryRoad.getActualDistance();
        dto.actualDuration = deliveryRoad.getActualDuration();
        dto.currentStatus = deliveryRoad.getCurrentStatus();
        return dto;
    }
}