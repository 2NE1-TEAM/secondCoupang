package com.toanyone.hub.infrastructure.init;

import com.toanyone.hub.domain.model.Address;
import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.model.HubDistance;
import com.toanyone.hub.domain.model.Location;
import com.toanyone.hub.domain.repository.HubDistanceRepository;
import com.toanyone.hub.domain.repository.HubRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
public class HubDataInitializer {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final HubRepository hubRepository;
    private final HubDistanceRepository hubDistanceRepository;
    private final Map<String, Long> hubs = new ConcurrentHashMap<>();

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    @Transactional
    public void init() {
        if (hubRepository.count() == 0) {
            insertHubs();
            insertHubDistances(); // 기존 허브들 거리 정보 저장
        }
    }

    /**
     * 📌 허브 정보 삽입
     */
    private void insertHubs() {
        hubs.put("서울", saveHub("서울특별시 센터", 37.514575, 127.105399, "서울특별시 송파구 송파대로 55", "0212345678"));
        hubs.put("경기북부", saveHub("경기 북부 센터", 37.658873, 126.832973, "경기도 고양시 덕양구 권율대로 570", "03122345678"));
        hubs.put("경기남부", saveHub("경기 남부 센터", 37.279456, 127.545136, "경기도 이천시 덕평로 25721", "03132345678"));
        hubs.put("부산", saveHub("부산광역시 센터", 35.116535, 129.042189, "부산 동구 중앙대로 206", "05112345678"));
        hubs.put("대구", saveHub("대구광역시 센터", 35.885262, 128.611079, "대구 북구 태평로 161", "05322345678"));
        hubs.put("인천", saveHub("인천광역시 센터", 37.464531, 126.707896, "인천 남동구 정각로 29", "03212345678"));
        hubs.put("광주", saveHub("광주광역시 센터", 35.160172, 126.851515, "광주 서구 내방로 111", "06212345678"));
        hubs.put("대전", saveHub("대전광역시 센터", 36.351599, 127.378481, "대전 서구 둔산로 100", "04212345678"));
        hubs.put("울산", saveHub("울산광역시 센터", 35.538377, 129.311369, "울산 남구 중앙로 201", "05212345678"));
        hubs.put("세종", saveHub("세종특별자치시 센터", 36.487530, 127.282425, "세종특별자치시 한누리대로 2130", "04412345678"));
        hubs.put("강원", saveHub("강원특별자치도 센터", 37.882599, 127.734446, "강원특별자치도 춘천시 중앙로 1", "03312345678"));
        hubs.put("충북", saveHub("충청북도 센터", 36.635676, 127.491269, "충북 청주시 상당구 상당로 82", "04312345678"));
        hubs.put("충남", saveHub("충청남도 센터", 36.597889, 126.662531, "충남 홍성군 홍북읍 충남대로 21", "04112345678"));
        hubs.put("전북", saveHub("전북특별자치도 센터", 35.826185, 127.148526, "전북특별자치도 전주시 완산구 효자로 225", "06312345678"));
        hubs.put("전남", saveHub("전라남도 센터", 34.817113, 126.463046, "전남 무안군 삼향읍 오룡길 1", "06112345678"));
        hubs.put("경북", saveHub("경상북도 센터", 36.576020, 128.504620, "경북 안동시 풍천면 도청대로 455", "05412345678"));
        hubs.put("경남", saveHub("경상남도 센터", 35.227870, 128.681944, "경남 창원시 의창구 중앙대로 300", "05512345678"));
    }

    public Long saveHub(String name, double latitude, double longitude, String address, String telephone) {
        Hub hub = Hub.createHub(name, new Location(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude)), new Address(address), telephone);
        hubRepository.save(hub);
        return getLastInsertedId();
    }

    private Long getLastInsertedId() {
        return ((Number) entityManager.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult()).longValue();
    }

    /**
     *  기존 허브들 간 거리 정보 삽입
     */
    private void insertHubDistances() {
        List<Hub> allHubs = hubRepository.findAll();
        for (Hub startHub : allHubs) {
            for (Hub endHub : allHubs) {
                if (!startHub.equals(endHub)) {
                    int distanceKm = calculateDistance(startHub, endHub);
                    saveHubDistance(startHub.getId(), endHub.getId(), distanceKm);
                }
            }
        }
    }

    /**
     *  새로운 허브 추가 시 실행되는 로직
     */
    @Transactional
    public Hub addNewHub(Hub newHub) {
        //  새로운 허브 저장
        hubRepository.save(newHub);
        hubs.put(newHub.getHubName(), newHub.getId());

        //  기존 허브들과의 거리 정보 추가
        insertHubDistancesForNewHub(newHub);

        return newHub;
    }

    /**
     *  새로운 허브에 대해 기존 허브들과 거리 정보 추가
     */
    private void insertHubDistancesForNewHub(Hub newHub) {
        List<Hub> existingHubs = hubRepository.findAll();

        for (Hub existingHub : existingHubs) {
            if (!existingHub.equals(newHub)) {
                int distanceKm = calculateDistance(newHub, existingHub);
                saveHubDistance(newHub.getId(), existingHub.getId(), distanceKm);
            }
        }
    }

    /**
     *  허브 간 거리 정보 저장 (중복 방지)
     */
    private void saveHubDistance(Long startHubId, Long endHubId, int distanceKm) {
        boolean exists = hubDistanceRepository.existsByStartHubIdAndEndHubId(startHubId, endHubId);
        if (exists) return; // 기존 데이터가 있으면 추가하지 않음

        Hub startHub = hubRepository.findById(startHubId).orElseThrow();
        Hub endHub = hubRepository.findById(endHubId).orElseThrow();

        hubDistanceRepository.save(new HubDistance(startHub, endHub, distanceKm, estimateTravelTime(distanceKm)));
        hubDistanceRepository.save(new HubDistance(endHub, startHub, distanceKm, estimateTravelTime(distanceKm)));
    }

    /**
     *  두 허브 간의 거리 계산 (Haversine 공식 사용)
     */
    private int calculateDistance(Hub start, Hub end) {
        double lat1 = Math.toRadians(start.getLocation().getLatitude().doubleValue());
        double lon1 = Math.toRadians(start.getLocation().getLongitude().doubleValue());
        double lat2 = Math.toRadians(end.getLocation().getLatitude().doubleValue());
        double lon2 = Math.toRadians(end.getLocation().getLongitude().doubleValue());

        // Haversine 공식 적용
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = EARTH_RADIUS_KM * c;

        return (int) Math.round(distanceKm * 1.3); // 도로 사정 고려 (30% 증가)
    }

    /**
     *  거리(km)에 따른 이동 시간(분) 추정
     */
    private int estimateTravelTime(double distanceKm) {
        double speedKmH;
        if (distanceKm < 10) {
            speedKmH = 40; // 도시 내 저속 이동
        } else if (distanceKm < 50) {
            speedKmH = 60; // 일반 도로
        } else {
            speedKmH = 80; // 고속도로
        }
        return (int) Math.round((distanceKm / speedKmH) * 60); // 시간(h) -> 분(min) 변환
    }
}