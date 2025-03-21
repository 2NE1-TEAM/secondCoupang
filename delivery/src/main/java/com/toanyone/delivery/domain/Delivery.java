package com.toanyone.delivery.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "p_delivery")
public class Delivery extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id", nullable = false)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    @OneToMany(mappedBy = "delivery", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<DeliveryRoad> deliveryRoads = new ArrayList<DeliveryRoad>();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @Column(nullable = false)
    private Long departureHubId;

    @Column(nullable = false)
    private Long arrivalHubId;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private Long recipientSlackId;

    @Column(nullable = false)
    private Long storeDeliveryManagerId;

    public static Delivery createDelivery(Long orderId, List<DeliveryRoad> deliveryRoads, Long departureHubId, Long arrivalHubId, String deliveryAddress, final String recipient, Long recipientSlackId, Long storeDeliveryManagerId) {
        Delivery delivery = new Delivery();
        delivery.orderId = orderId;
        delivery.deliveryRoads = deliveryRoads;
        delivery.departureHubId = departureHubId;
        delivery.arrivalHubId = arrivalHubId;
        delivery.deliveryAddress = deliveryAddress;
        delivery.deliveryStatus = DeliveryStatus.HUB_WAITING;
        delivery.recipient = recipient;
        delivery.recipientSlackId = recipientSlackId;
        delivery.storeDeliveryManagerId = storeDeliveryManagerId;
        delivery.getDeliveryRoads()
                .forEach(deliveryRoad -> deliveryRoad.addDelivery(delivery));
        return delivery;
    }

    public void updateDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public void deliverDelivery(Long deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    @Getter
    public enum DeliveryStatus {
        HUB_WAITING("허브 이동 대기중"),
        HUB_MOVING("허브 이동중"),
        DESTINATION_HUB_ARRIVED("목적지 허브 도착"),
        DELIVERING("배송중"),
        STORE_MOVING("업체로 이동중"),
        DELIVERY_COMPLETED("배송완료");

        private final String value;

        DeliveryStatus(String value) {
            this.value = value;
        }

        public static Optional<Delivery.DeliveryStatus> fromValue(String value) {
            for (Delivery.DeliveryStatus type : values()) {
                if (type.value.equals(value)) {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }
    }


}
