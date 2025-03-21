package com.toanyone.hub.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_hub")
@ToString
@Where(clause = "deleted_at IS NULL")
public class Hub extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hub_id", unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String hubName;

    @Embedded
    private Location location;

    @Embedded
    private Address address;

    private String telephone;

    @OneToMany(mappedBy = "startHub", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HubDistance> hubDistances;

    public static Hub createHub(String name, Location location, Address address, String telephone) {
        Hub hub = new Hub();
        hub.hubName = name;
        hub.location = location;
        hub.address = address;
        hub.telephone = telephone;
        return hub;
    }
}