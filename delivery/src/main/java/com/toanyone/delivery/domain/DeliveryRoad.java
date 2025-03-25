package com.toanyone.delivery.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "p_delivery_road")
public class DeliveryRoad extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_road_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @Column(nullable = false)
    private Long deliveryManagerId;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private Long departureHubId;

    @Column(nullable = false)
    private Long arrivalHubId;

    @Column(nullable = false)
    private BigDecimal estimatedDistance;

    @Column(nullable = false)
    private int estimatedDuration;

    private BigDecimal actualDistance;

    private int actualDuration;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrentStatus currentStatus;

    public static DeliveryRoad createDeliveryRoad(Long deliveryManagerId, int sequence, Long departureHubId, Long arrivalHubId,
                                                  BigDecimal estimatedDistance, int estimatedDuration) {
        DeliveryRoad deliveryRoad = new DeliveryRoad();
        deliveryRoad.deliveryManagerId = deliveryManagerId;
        deliveryRoad.sequence = sequence;
        deliveryRoad.departureHubId = departureHubId;
        deliveryRoad.arrivalHubId = arrivalHubId;
        deliveryRoad.estimatedDistance = estimatedDistance;
        deliveryRoad.estimatedDuration = estimatedDuration;
        deliveryRoad.currentStatus = CurrentStatus.HUB_WAITING;
        return deliveryRoad;
    }


    public void deleteDeliveryRoad(Long deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    public void addDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public void updateActualDistance(BigDecimal actualDistance) {
        this.actualDistance = actualDistance;
    }

    public void updatedActualDuration(int actualDuration) {
        this.actualDuration = actualDuration;
    }

    @Getter
    public enum CurrentStatus {
        HUB_WAITING("허브 이동 대기중"),
        HUB_MOVING("허브 이동중"),
        DESTINATION_HUB_ARRIVED("목적지 허브 도착"),
        DELIVERING("배송중"),
        DELIVERY_COMPLETED("배송완료");

        private final String value;

        CurrentStatus(String value) {
            this.value = value;
        }
    }

}
