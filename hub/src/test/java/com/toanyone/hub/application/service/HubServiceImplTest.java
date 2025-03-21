package com.toanyone.hub.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.repository.HubRepository;
import com.toanyone.hub.domain.service.HubService;
import com.toanyone.hub.domain.service.RouteService;
import com.toanyone.hub.presentation.dto.CursorPage;
import com.toanyone.hub.presentation.dto.HubFindResponseDto;
import com.toanyone.hub.presentation.dto.HubSearchRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class HubServiceImplTest {

    @Autowired
    HubService hubService;

    @Autowired
    HubRepository hubRepository;

    @Autowired
    RouteService routeService;

    @Test
    @DisplayName("허브 검색 및 커서페이징 테스트")
    void test() throws JsonProcessingException {
        CursorPage<HubFindResponseDto> hubs =
                hubService.findHubs(HubSearchRequest.builder().keyword("부산").build(), "createdAt", "desc", 10);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

//        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(hubs));
        assertThat(hubs.getContent().get(0).getHubName()).isEqualTo("부산광역시 센터"); //커서페이징 잘 되는지
//        assertThat(hubs.isHasNext()).isTrue(); // 다음페이지 존재 검증
    }

    @Test
    @DisplayName("허브 단건 조회 테스트")
    void test2()  {
        Hub hub = hubRepository.findById(1L).orElseThrow();
        assertThat(hub.getHubName()).isEqualTo("서울특별시 센터");
    }
}