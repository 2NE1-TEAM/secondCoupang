package com.toanyone.store.presentation.dto;

import com.toanyone.store.common.util.PhoneNumberUtils;
import com.toanyone.store.domain.model.DetailAddress;
import com.toanyone.store.domain.model.Location;
import com.toanyone.store.domain.model.Store;
import com.toanyone.store.domain.model.StoreType;
import lombok.*;

import java.io.Serializable;

@Getter
public class StoreFindResponseDto implements Serializable {
    private Long storeId;
    private String storeName;
    private StoreType storeType;
    private Location location;
    private DetailAddress detailAddress;
    private Long hubId;
    private String hubName;
    private String telephone;

    public static StoreFindResponseDto of(Store store, Long hubId, String hubName) {
        StoreFindResponseDto storeFindResponseDto = new StoreFindResponseDto();
        storeFindResponseDto.storeId = store.getId();
        storeFindResponseDto.storeName = store.getStoreName();
        storeFindResponseDto.storeType = store.getStoreType();
        storeFindResponseDto.location = store.getLocation();
        storeFindResponseDto.detailAddress = store.getDetailAddress();
        storeFindResponseDto.hubName = hubName;
        storeFindResponseDto.telephone = PhoneNumberUtils.formatPhoneNumber(store.getTelephone()); // 전화번호 "-" 추가
        storeFindResponseDto.hubId = hubId;
        return storeFindResponseDto;
    }
}
