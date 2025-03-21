package com.toanyone.hub.infrastructure.init;

import com.toanyone.hub.domain.model.HubDistance;
import com.toanyone.hub.domain.repository.HubDistanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;



/**
 * 초기 카카오API로 각 허브 거리 가져온 걸 SQL문으로 변경하기 위한 코드
 */
//@Component
@RequiredArgsConstructor
public class HubDistanceSqlExporter {

    private final HubDistanceRepository hubDistanceRepository;

    public void exportToSqlFile() {
        List<HubDistance> distances = hubDistanceRepository.findAll();

        try (FileWriter writer = new FileWriter("src/main/resources/import.sql")) {
            writer.write("허브 거리 데이터 초기화");
            for (HubDistance distance : distances) {
                String sql = String.format(
                        "INSERT INTO hub_distance (start_hub_id, end_hub_id, distance_km, estimated_time) VALUES (%d, %d, %d, %d);\n",
                        distance.getStartHub().getId(),
                        distance.getEndHub().getId(),
                        distance.getDistanceKm(),
                        distance.getEstimatedTime()
                );
                writer.write(sql);
            }
            System.out.println(" import.sql 생성 완료!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}