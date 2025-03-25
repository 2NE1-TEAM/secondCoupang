package com.toanyone.delivery.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.toanyone.delivery.domain.Delivery;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class GetDeliveryResponseDto {

    @JsonProperty("delivery_id")
    private Long deliveryId;

    @JsonProperty("departure_hub_id")
    private Long departureHubId;

    @JsonProperty("arrival_hub_id")
    private Long arrivalHubId;

    @JsonProperty("delivery_address")
    private String deliveryAddress;

    @JsonProperty("delivery_roads")
    private List<GetDeliveryRoadResponseDto> deliveryRoadResponseDtos;

    @JsonProperty("recipient")
    private String recipient;

    @JsonProperty("recipient_slack_id")
    private String recipientSlackId;

    @JsonProperty("store_delivery_manager_id")
    private Long storeDeliveryManagerId;

    @JsonProperty("order_id")
    private Long orderId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("created_by")
    private Long createdBy;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("updated_by")
    private Long updatedBy;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;

    @JsonProperty("deleted_by")
    private Long deletedBy;

    public static GetDeliveryResponseDto from(Delivery delivery) {
        GetDeliveryResponseDto dto = new GetDeliveryResponseDto();
        dto.deliveryId = delivery.getId();
        dto.departureHubId = delivery.getDepartureHubId();
        dto.arrivalHubId = delivery.getArrivalHubId();
        dto.deliveryAddress = delivery.getDeliveryAddress();
        dto.recipient = delivery.getRecipient();
        dto.recipientSlackId = delivery.getRecipientSlackId();
        dto.storeDeliveryManagerId = delivery.getStoreDeliveryManagerId();
        dto.orderId = delivery.getOrderId();
        dto.createdAt = delivery.getCreatedAt();
        dto.createdBy = delivery.getCreatedBy();
        dto.updatedAt = delivery.getUpdatedAt();
        dto.updatedBy = delivery.getUpdatedBy();
        dto.deletedAt = delivery.getDeletedAt();
        dto.deletedBy = delivery.getDeletedBy();
        dto.deliveryRoadResponseDtos = delivery.getDeliveryRoads()
                .stream()
                .map(GetDeliveryRoadResponseDto::from)
                .collect(Collectors.toList());
        return dto;
    }

}

