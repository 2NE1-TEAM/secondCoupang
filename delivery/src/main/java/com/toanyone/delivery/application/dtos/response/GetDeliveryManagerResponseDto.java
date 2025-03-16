package com.toanyone.delivery.application.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.toanyone.delivery.domain.DeliveryManager;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetDeliveryManagerResponseDto {
    @JsonProperty("delivery_manager_id")
    private Long deliveryManagerId;

    @JsonProperty("delivery_manager_type")
    private DeliveryManager.DeliveryManagerType deliveryManagerType;

    @JsonProperty("hub_id")
    private Long hubId;

    @JsonProperty("delivery_order")
    private Long deliveryOrder;

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

    public static GetDeliveryManagerResponseDto from(DeliveryManager deliveryManager) {
        GetDeliveryManagerResponseDto dto = new GetDeliveryManagerResponseDto();
        dto.deliveryManagerId = deliveryManager.getId();
        dto.deliveryManagerType = deliveryManager.getDeliveryManagerType();
        dto.hubId = deliveryManager.getHubId();
        dto.deliveryOrder = deliveryManager.getDeliveryOrder();
        dto.createdAt = deliveryManager.getCreatedAt();
        dto.createdBy = deliveryManager.getCreatedBy();
        dto.updatedAt = deliveryManager.getUpdatedAt();
        dto.updatedBy = deliveryManager.getUpdatedBy();
        dto.deletedAt = deliveryManager.getDeletedAt();
        dto.deletedBy = deliveryManager.getDeletedBy();
        return dto;
    }
}
