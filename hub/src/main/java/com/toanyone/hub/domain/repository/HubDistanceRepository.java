package com.toanyone.hub.domain.repository;

import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.model.HubDistance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubDistanceRepository extends JpaRepository<HubDistance, Long> {
    boolean existsByStartHubIdAndEndHubId(Long startHubId, Long endHubId);
}
