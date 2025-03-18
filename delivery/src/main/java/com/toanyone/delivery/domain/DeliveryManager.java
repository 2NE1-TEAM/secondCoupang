package com.toanyone.delivery.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "p_delivery_manager")
public class DeliveryManager extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_manager_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private DeliveryManagerType deliveryManagerType;

    @Column(nullable = false)
    private Long hubId;

    @Column(nullable = false)
    private Long deliveryOrder;

    @Column(nullable = false)
    private String name;

    public static DeliveryManager createDeliveryManager(Long userId, DeliveryManagerType deliveryManagerType, Long hubId, Long deliveryOrder, String name) {
        DeliveryManager deliveryManager = new DeliveryManager();
        deliveryManager.userId = userId;
        deliveryManager.deliveryManagerType = deliveryManagerType;
        deliveryManager.hubId = hubId;
        deliveryManager.deliveryOrder = deliveryOrder;
        deliveryManager.name = name;
        return deliveryManager;
    }

    public void deleteDeliveryManager(Long deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    @Getter
    public enum DeliveryManagerType {
        HUB_DELIVERY_MANAGER("허브 배송 담당자"),
        STORE_DELIVERY_MANAGER("업체 배송 담당자");

        private final String value;

        DeliveryManagerType(String value) {
            this.value = value;
        }

        public static Optional<DeliveryManagerType> fromValue(String value) {
            for (DeliveryManagerType type : values()) {
                if (type.value.equals(value)) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }
    }


}
