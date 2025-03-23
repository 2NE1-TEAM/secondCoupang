package com.toanyone.hub.domain.repository;

import com.toanyone.hub.domain.model.Address;
import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.model.Location;
import com.toanyone.hub.infrastructure.repository.HubRepositoryCustom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HubRepository extends JpaRepository<Hub, Long>, HubRepositoryCustom {
    boolean existsByHubName(String hubName);

    boolean existsByLocation(Location location);

    boolean existsByAddress(Address address);

    boolean existsByTelephone(String telephone);

    @EntityGraph(attributePaths = {"hubDistances"})
    Optional<Hub> findFetchById(Long hubId);

    boolean existsByTelephoneAndIdNot(String telephone, Long storeId);

    boolean existsByHubNameAndIdNot(String hubName, Long id);

}