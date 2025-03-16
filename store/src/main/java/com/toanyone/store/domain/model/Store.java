package com.toanyone.store.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "P_STORE")
public class Store extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id", unique = true, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreType storeType; // 생산업체인지, 수령업체인지

    @Column(nullable = false)
    private String storeName; // 업체 이름

    @Column(nullable = false)
    private Long hubId; // 업체가 등록된 허브의 ID

    @Column(nullable = false)
    private DetailAddress detailAddress; // 상세주소

    @Column(nullable = false)
    private String telephone; // 전화번호

    @Column(nullable = false)
    private Location location; // 위도, 경도

    /**
     * 업체를 생성하는 정적 팩토리 메서드
     */
    public static Store create(String storeName, Long hubId, DetailAddress detailAddress, Location location, StoreType storeType, String telephone) {
        Store store = new Store();
        store.storeType = storeType;
        store.storeName = storeName;
        store.hubId = hubId;
        store.detailAddress = detailAddress;
        store.location = location;
        store.telephone = telephone;
        return store;
    }
}
