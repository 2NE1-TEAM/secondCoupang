package com.toanyone.delivery.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "p_delivery")
public class Delivery extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @Column(name = "delivery_id")
    private Long id;
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "delivery_status")
    @NotNull
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;
    @Column(name = "departure_hub_id")
    @NotNull
    private Long departureHubId;
    @Column(name = "arrival_hub_id")
    @NotNull
    private Long arrivalHubId;
    @Column(name = "delivery_address")
    @NotNull
    private String deliveryAddress;
    @NotNull
    private String recipient;
    @Column(name = "recipient_slack_id")
    @NotNull
    private Long recipientSlackId;
    @Column(name = "store_delivery_manager_id")
    @NotNull
    private Long storeDeliveryManagerId;

    public static Delivery createDelivery(Long orderId, Long departureHubId, Long arrivalHubId, String deliveryAddress, final String recipient, Long recipientSlackId, Long storeDeliveryManagerId) {
        Delivery delivery = new Delivery();
        delivery.orderId = orderId;
        delivery.departureHubId = departureHubId;
        delivery.arrivalHubId = arrivalHubId;
        delivery.deliveryAddress = deliveryAddress;
        delivery.deliveryStatus = DeliveryStatus.HUB_WAITING;
        delivery.recipient = recipient;
        delivery.recipientSlackId = recipientSlackId;
        delivery.storeDeliveryManagerId = storeDeliveryManagerId;
        return delivery;
    }

    public enum DeliveryStatus {
        HUB_WAITING,
        HUB_MOVING,
        DESTINATION_HUB_ARRIVED,
        DELIVERING,
        STORE_MOVING,
        DELIVERY_COMPLETED,
        ;
    }


}
