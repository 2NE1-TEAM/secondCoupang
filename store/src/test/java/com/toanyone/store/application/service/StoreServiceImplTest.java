package com.toanyone.store.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toanyone.store.common.filter.UserContext;
import com.toanyone.store.domain.model.DetailAddress;
import com.toanyone.store.domain.model.Location;
import com.toanyone.store.domain.model.Store;
import com.toanyone.store.domain.model.StoreType;
import com.toanyone.store.domain.repository.StoreRepository;
import com.toanyone.store.domain.service.StoreService;
import com.toanyone.store.infrastructure.client.HubClient;
import com.toanyone.store.infrastructure.client.dto.HubResponseDto;
import com.toanyone.store.presentation.dto.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@SpringBootTest
@Transactional
class StoreServiceImplTest {

    @Autowired
    UserContext userContext;

    @Autowired
    StoreRepository storeRepository;

    @Autowired  // @InjectMocks → @Autowired
    private StoreService storeService;

    @MockBean  // @Mock 대신 @MockBean 사용
    private HubClient hubClient;

    @Autowired
    EntityManager em;

    @TestConfiguration
    public class TestAuditorAwareConfig {
        @Bean
        public AuditorAware<Long> auditorAware() {
            return () -> Optional.of(999L); // 테스트용 사용자 ID
        }
    }

    @BeforeEach
    void setUp() {
        // 테스트 실행 전에 UserContext에 임의의 사용자 정보 설정
        UserInfo mockUser = new UserInfo(1L, "ROLE_USER", "xxxx"); // 사용자 ID: 1, 역할: ROLE_USER
        userContext.setUser(mockUser);
    }

    @Test
    @DisplayName("업체 생성 테스트")
    void StoreCreateTest() {
        //given
        HubResponseDto mockHubResponse = new HubResponseDto(1L, "허브 A", new DetailAddress("a동 102동"),
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)), 1L);
        when(hubClient.getHubById(1L)).thenReturn(ResponseEntity.ok(SingleResponse.success(mockHubResponse)));

        StoreCreateRequestDto requestDto = new StoreCreateRequestDto(
                "찬이네 서울 정육점", StoreType.PRODUCER,
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)),
                new DetailAddress("102동 1201호"), 1L, "02-123-4567");

        //then
        StoreCreateResponseDto storeCreateResponseDto = storeService.createStore(requestDto);
        Store findStore = storeRepository.findById(storeCreateResponseDto.getStoreId()).orElseThrow(RuntimeException::new);

        assertThat(storeCreateResponseDto.getStoreId()).isEqualTo(findStore.getId());
    }

    @Test
    @DisplayName("업체 단건 조회 테스트")
    void storeFindOneTest() {
        // given
        HubResponseDto mockHubResponse = new HubResponseDto(1L, "허브 A", new DetailAddress("a동 102동"),
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)), 1L);
        when(hubClient.getHubById(1L)).thenReturn(ResponseEntity.ok(SingleResponse.success(mockHubResponse)));

        StoreCreateRequestDto requestDto = new StoreCreateRequestDto(
                "찬이네 서울 정육점", StoreType.PRODUCER,
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)),
                new DetailAddress("102동 1201호"), 1L, "02-123-4567");
        StoreCreateResponseDto storeCreateResponseDto = storeService.createStore(requestDto);

        // when
        StoreFindResponseDto response = storeService.findOne(storeCreateResponseDto.getStoreId());

        // then
        assertThat(response.getStoreName()).isEqualTo("찬이네 서울 정육점");
        assertThat(response.getHubName()).isEqualTo("허브 A");
    }

    @Test
    @DisplayName("업체 삭제 테스트")
    void storeDeleteTest() {
        // given
        Store savedStore = storeRepository.save(Store.create("찬이네 서울 정육점", 1L, new DetailAddress("102동 1201호"),
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)),
                StoreType.PRODUCER, "02-123-4567", "서울허브"));

        // when
        savedStore.delete(userContext.getUser().getUserId());

        Store findStore = storeRepository.findById(savedStore.getId()).get();

        // then
        assertThat(findStore.getDeletedAt()).isNotNull(); // 삭제날짜 들어가는지
        assertThat(findStore.getDeletedBy()).isEqualTo(1L); // 쓰레드로컬에서 유저정보 잘 가져오는지 확인
    }

    @Test
    @DisplayName("스토어 업데이트 테스트")
    void storeUpdateTest() {
        //given

        HubResponseDto mockHubResponse = new HubResponseDto(1L, "허브 A", new DetailAddress("a동 102동"),
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)), 1L);
        when(hubClient.getHubById(1L)).thenReturn(ResponseEntity.ok(SingleResponse.success(mockHubResponse)));


        Store savedStore = storeRepository.save(Store.create("찬이네 서울 정육점", 1L, new DetailAddress("102동 1201호"),
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)),
                StoreType.PRODUCER, "021234567", "서울허브"));

        //when
        storeService.updateStore(savedStore.getId(), new StoreUpdateRequestDto(
                null, StoreType.CONSUMER,
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)),
                null, 1L, "02-123-1111"));

        em.flush();
        em.clear();

        Store findStore = storeRepository.findById(savedStore.getId()).get();

        //then
        assertThat(findStore.getStoreName()).isNotNull();
        assertThat(findStore.getDetailAddress()).isNotNull();

        assertThat(findStore.getTelephone()).isEqualTo("021231111");
    }

    @Test
    @DisplayName("스토어 검색 테스트")
    void storeSearchTest() throws JsonProcessingException {
        // given
        HubResponseDto mockHubResponse = new HubResponseDto(1L, "허브 A", new DetailAddress("a동 102동"),
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)), 1L);
        when(hubClient.getHubById(1L)).thenReturn(ResponseEntity.ok(SingleResponse.success(mockHubResponse)));

        for (int i = 1; i < 9; i++) {
            StoreCreateRequestDto requestDto = new StoreCreateRequestDto(
                    "찬이네 서울 정육점" + i, StoreType.PRODUCER,
                    new Location(new BigDecimal("123.123456"+i).setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.123456"+i).setScale(7, RoundingMode.HALF_UP)),
                    new DetailAddress("102동 1201호"), 1L, "02-123-4567" + i);
            storeService.createStore(requestDto);

//            StoreCreateRequestDto requestDto2 = new StoreCreateRequestDto(
//                    "수미네 서울 정육점" + i, StoreType.PRODUCER,
//                    new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)),
//                    new DetailAddress("102동 1201호"), 1L, "010-1212-3434" + i);
//            storeService.createStore(requestDto2);
        }

        // when
        CursorPage<StoreFindResponseDto> stores = storeService.findStores(StoreSearchRequest.builder().build(), "storeName", "desc", 10);

        // then
        assertThat(stores.getContent().get(0).getStoreName()).contains("찬이네 서울 정육점"); // 가게이름 + 전화번호 검색 잘 되는지
        assertThat(stores.getContent().size()).isEqualTo(10); // 요청한 사이즈대로 잘 가져오는지
//        assertThat(stores.getNextCursor().getLastStoreId()).isEqualTo(10L); //커서페이징 잘 되는지
//        assertThat(stores.getNextCursor().getLastStoreName()).isEqualTo("찬이네 서울 정육점9"); //커서페이징 잘 되는지
        assertThat(stores.isHasNext()).isTrue(); // 커서페이징에서 다음 페이지 존재하는 지 확인
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
//
//        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(stores));
    }
}