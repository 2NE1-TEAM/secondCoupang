package com.toanyone.hub.application.service;

import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.model.HubDistance;
import com.toanyone.hub.domain.repository.HubDistanceRepository;
import com.toanyone.hub.domain.repository.HubRepository;
import com.toanyone.hub.domain.service.RouteService;
import com.toanyone.hub.presentation.dto.RouteDTO;
import com.toanyone.hub.presentation.dto.RouteSegmentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RouteServiceImpl implements RouteService {

    private static final int MAX_DIRECT_DISTANCE_KM = 200; // 200km 이상이면 중간 경유지 필요

    private final HubRepository hubRepository;
    private final HubDistanceRepository hubDistanceRepository;
    private final KakaoDirectionService kakaoDirectionService;

    @Cacheable(value = "routeCache", key = "T(String).valueOf(#startHubId).concat('-').concat(T(String).valueOf(#endHubId))")
    public List<RouteSegmentDto> findShortestPath(Long startHubId, Long endHubId) {
        Hub startHub = hubRepository.findById(startHubId)
                .orElseThrow(() -> new RuntimeException("출발 허브를 찾을 수 없음"));
        Hub endHub = hubRepository.findById(endHubId)
                .orElseThrow(() -> new RuntimeException("도착 허브를 찾을 수 없음"));

        // 허브 정보를 그래프로 변환
        Map<Long, List<HubDistance>> graph = buildGraph();

        // 다익스트라 실행하여 최단 경로 찾기
        List<HubDistance> shortestPath = dijkstra(graph, startHub, endHub);

        // 거리 & 예상 시간 포함한 응답 변환
        return shortestPath.stream()
                .map(RouteSegmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     *  새로운 허브 추가 시, 기존 허브들과의 거리를 계산하여 저장
     */
    @Transactional
    public void addHubDistances(Hub newHub) {
        List<Hub> existingHubs = hubRepository.findAll();
        existingHubs.remove(existingHubs.size()-1);
//        System.out.println("리스너 실패");
//        throw new RuntimeException("리스너 실패");
        try{
            for (Hub existingHub : existingHubs) {
                if (!existingHub.equals(newHub)) {
                    double longitude = newHub.getLocation().getLongitude().doubleValue();
                    double latitude = newHub.getLocation().getLatitude().doubleValue();
                    double existingHubLongitude = existingHub.getLocation().getLongitude().doubleValue();
                    double existingHubLatitude= existingHub.getLocation().getLatitude().doubleValue();
                    RouteDTO routeDto = kakaoDirectionService.getDirections(longitude, latitude, existingHubLongitude, existingHubLatitude);
                    if (routeDto != null && routeDto.getRoutes() != null && routeDto.getRoutes().length > 0) {
                        RouteDTO.Route route = routeDto.getRoutes()[0];  // 첫 번째 경로 선택
                        long distance = route.getSummary().getDistance();  // 거리
                        long duration = route.getSummary().getDuration();  // 시간

                        // 거리를 기준으로 HubDistance 저장
                        hubDistanceRepository.save(new HubDistance(newHub, existingHub, (int) distance/1000, (int) duration/60));
                        hubDistanceRepository.save(new HubDistance(existingHub, newHub, (int) distance/1000, (int) duration/60));
                    } else {
                        log.error("경로 정보가 없습니다.");
                    }
                }
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     *  HubDistance 테이블을 기반으로 그래프 생성
     */
    private Map<Long, List<HubDistance>> buildGraph() {
        List<HubDistance> distances = hubDistanceRepository.findAll();
        Map<Long, List<HubDistance>> graph = new HashMap<>();

        for (HubDistance distance : distances) {
            if (distance.getDistanceKm() > MAX_DIRECT_DISTANCE_KM) {
                continue; //  200km 이상이면 직접 연결된 경로를 사용하지 않음
            }
            graph.computeIfAbsent(distance.getStartHub().getId(), k -> new ArrayList<>()).add(distance);
            graph.computeIfAbsent(distance.getEndHub().getId(), k -> new ArrayList<>()).add(
                    new HubDistance(distance.getEndHub(), distance.getStartHub(),
                            distance.getDistanceKm(), distance.getEstimatedTime()));
        }
        return graph;
    }

    private List<HubDistance> dijkstra(Map<Long, List<HubDistance>> graph, Hub start, Hub end) {
        Map<Long, Integer> distances = new HashMap<>();
        Map<Long, HubDistance> previous = new HashMap<>();
        PriorityQueue<HubDistance> queue = new PriorityQueue<>(Comparator.comparingInt(HubDistance::getDistanceKm));

        //  초기화
        for (Long hubId : graph.keySet()) {
            distances.put(hubId, Integer.MAX_VALUE);
        }
        distances.put(start.getId(), 0);
        queue.add(new HubDistance(start, start, 0, 0));

        while (!queue.isEmpty()) {
            HubDistance current = queue.poll();
            Long currentHubId = current.getEndHub().getId();

            if (currentHubId.equals(end.getId())) {
                break;
            }

            for (HubDistance neighbor : graph.getOrDefault(currentHubId, Collections.emptyList())) {
                int newDist = distances.get(currentHubId) + neighbor.getDistanceKm();
                if (newDist < distances.get(neighbor.getEndHub().getId())) {
                    distances.put(neighbor.getEndHub().getId(), newDist);
                    previous.put(neighbor.getEndHub().getId(), neighbor);
                    queue.add(new HubDistance(neighbor.getStartHub(), neighbor.getEndHub(),
                            newDist, neighbor.getEstimatedTime()));
                }
            }
        }

        //  최단 경로 복원
        List<HubDistance> path = new ArrayList<>();
        Long step = end.getId();

        while (step != null && previous.containsKey(step)) {
            HubDistance segment = previous.get(step);
            path.add(segment);
            step = segment.getStartHub().getId();
        }

        Collections.reverse(path); // 역순 정렬
        return path;
    }
}