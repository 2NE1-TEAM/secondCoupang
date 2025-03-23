package com.toanyone.hub.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toanyone.hub.domain.model.Address;
import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.model.Location;
import com.toanyone.hub.domain.service.HubService;
import com.toanyone.hub.domain.service.RouteService;
import com.toanyone.hub.infrastructure.init.HubDataInitializer;
import com.toanyone.hub.infrastructure.init.HubDistanceSqlExporter;
import com.toanyone.hub.presentation.dto.HubCreateRequestDto;
import com.toanyone.hub.presentation.dto.HubDto;
import com.toanyone.hub.presentation.dto.RouteSegmentDto;
import com.toanyone.hub.presentation.dto.SingleResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = true)
class RouteServiceImplTest {

    @Autowired
    private RouteService routeService;

    @Autowired
    private ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper 주입

    @Autowired
    private HubDataInitializer hubDataInitializer;

//    @Autowired
//    private HubDistanceSqlExporter hubDistanceSqlExporter;

    @Autowired
    EntityManager em;
    @Autowired
    private HubService hubService;

    @Test
    void test() throws JsonProcessingException {
        List<RouteSegmentDto> shortestPath = routeService.findShortestPath(4L, 1L);
        SingleResponse<List<RouteSegmentDto>> success = SingleResponse.success(shortestPath);
        String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(success);
        System.out.println(jsonResult);
//        Hub jejuHub = new Hub("제주도", new Location(BigDecimal.valueOf(126.5311884), BigDecimal.valueOf(33.4996213)), new Address("제주특별시 제주대로 123길"), "01-222-2222");
//        hubDataInitializer.saveHub("제주도", 126.5311884, 33.4996213, "제주특별시 제주대로 123길", "01-222-2222");
//        hubDataInitializer.insertHubDistances();
//        List<RouteSegmentDto> shortestPath = routeService.findShortestPath("제주도", "부산");
//        SingleResponse<List<RouteSegmentDto>> success = SingleResponse.success(shortestPath);
//        String jsonResult = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(success);
//        System.out.println(jsonResult);


    }

//    @Test
//    void test2() throws JsonProcessingException {
//        hubDataInitializer.addNewHub(new Hub("제주도", new Location(BigDecimal.valueOf(126.5311884), BigDecimal.valueOf(33.4996213)), new Address("제주특별시 제주대로 123길"), "01-222-2222"));
//    }

    @Test
    void test3() throws JsonProcessingException {
        //hubService.createHub(new HubCreateRequestDto("제주도", new Location(BigDecimal.valueOf(33.4996213), BigDecimal.valueOf(126.5311884)), new Address("제주특별시 제주대로 123길"), "01-222-2222"));
        List<RouteSegmentDto> shortestPath2 = routeService.findShortestPath(1L, 4L);
        String jsonResult2 = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(shortestPath2);
        System.out.println("123213");
        System.out.println(jsonResult2); // JSON 형태로 출력
        System.out.println("123213");
    }
}