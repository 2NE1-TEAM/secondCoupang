package com.toanyone.store.infrastructure.init;

import com.toanyone.store.domain.model.DetailAddress;
import com.toanyone.store.domain.model.Location;
import com.toanyone.store.domain.model.Store;
import com.toanyone.store.domain.model.StoreType;
import com.toanyone.store.domain.repository.StoreRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@RequiredArgsConstructor
@Component
public class StoreDataInitializer {

    private final StoreRepository storeRepository;

    @PostConstruct
    @Transactional
    public void init() {
        insertStores();
    }
    private void insertStores() {
//        Store store = Store.create("찬이네 서울 정육점", 1L, new DetailAddress("102동 1201호"),
//                new Location(new BigDecimal("37.514575").setScale(7, RoundingMode.HALF_UP), new BigDecimal("127.0495556").setScale(7, RoundingMode.HALF_UP)),
//                StoreType.PRODUCER, "021234567", "서울특별시 센터");
//        storeRepository.save(store);

        for (int i = 1; i <= 100; i++) {
            String[] hubNames = {
                    "서울특별시 센터", "경기 북부 센터", "경기 남부 센터", "부산광역시 센터", "대구광역시 센터",
                    "인천광역시 센터", "광주광역시 센터", "대전광역시 센터", "울산광역시 센터", "세종특별자치시 센터",
                    "강원특별자치도 센터", "충청북도 센터", "충청남도 센터", "전북특별자치도 센터", "전라남도 센터",
                    "경상북도 센터", "경상남도 센터"
            };

            double[][] locations = {
                    {37.514575, 127.105399}, {37.658873, 126.832973}, {37.279456, 127.545136}, {35.116535, 129.042189},
                    {35.885262, 128.611079}, {37.464531, 126.707896}, {35.160172, 126.851515}, {36.351599, 127.378481},
                    {35.538377, 129.311369}, {36.487530, 127.282425}, {37.882599, 127.734446}, {36.635676, 127.491269},
                    {36.597889, 126.662531}, {35.826185, 127.148526}, {34.817113, 126.463046}, {36.576020, 128.504620},
                    {35.227870, 128.681944}
            };

            int idx = (int)(Math.random() * hubNames.length);
            String hubName = hubNames[idx];
            Long hubId = (long) (idx + 1);

            double baseLat = locations[idx][0];
            double baseLng = locations[idx][1];
            double offsetLat = Math.random() * 0.0005;
            double offsetLng = Math.random() * 0.0005;

            BigDecimal lat = new BigDecimal(baseLat + offsetLat).setScale(7, RoundingMode.HALF_UP);
            BigDecimal lng = new BigDecimal(baseLng + offsetLng).setScale(7, RoundingMode.HALF_UP);

            StoreType type = (i % 2 == 0) ? StoreType.PRODUCER : StoreType.CONSUMER;
            String storeName = "스토어" + i + "_" + hubName.split(" ")[0];
            String phone = String.format("02-1000-%04d", i);
            String detailAddr = (100 + i % 10) + "동 " + (1000 + i % 10) + "호";

            storeRepository.save(Store.create(storeName, hubId,
                    new DetailAddress(detailAddr),
                    new Location(lat, lng),
                    type,
                    phone,
                    hubName));
        }
    }
}