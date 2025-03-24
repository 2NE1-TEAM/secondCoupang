package com.toanyone.hub.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@Table(name = "p_hub_distance")
public class HubDistance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hub_distance_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_hub_id", nullable = false)
    private Hub startHub;  // 출발 허브

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_hub_id", nullable = false)
    private Hub endHub;  // 도착 허브

    @Column(nullable = false)
    private int distanceKm; // 허브 간 거리 (KM)

    @Column(nullable = false)
    private int estimatedTime; // 예상 소요 시간 (분)

    public HubDistance(Hub startHub, Hub endHub, int distanceKm, int estimatedTime) {
        this.startHub = startHub;
        this.endHub = endHub;
        this.distanceKm = distanceKm;
        this.estimatedTime = estimatedTime;
    }
}