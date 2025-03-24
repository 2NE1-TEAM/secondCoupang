package com.toanyone.store.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "P_STORE")
@SQLRestriction("deleted_at IS NULL")
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

    @Column(nullable = false)
    private String hubName;

    /**
     * 업체를 생성하는 정적 팩토리 메서드
     */
    public static Store create(String storeName, Long hubId, DetailAddress detailAddress, Location location, StoreType storeType, String telephone, String hubName) {
        Store store = new Store();
        store.storeType = storeType;
        store.storeName = storeName;
        store.hubId = hubId;
        store.detailAddress = detailAddress;
        store.location = location;
        store.telephone = telephone;
        store.hubName = hubName;
        return store;
    }

    public void updateStoreName(String storeName) {
        this.storeName = storeName;
    }

    public void updateStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public void updateLocation(Location location) {
        this.location = location;
    }

    public void updateDetailAddress(DetailAddress detailAddress) {
        this.detailAddress = detailAddress;
    }

    public void updateTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void updateHub(Long hubId, String hubName) {
        this.hubId = hubId;
        this.hubName = hubName;
    }
}
