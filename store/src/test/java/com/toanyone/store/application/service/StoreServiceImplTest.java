package com.toanyone.store.application.service;

import com.toanyone.store.domain.model.DetailAddress;
import com.toanyone.store.domain.model.Location;
import com.toanyone.store.domain.model.Store;
import com.toanyone.store.domain.model.StoreType;
import com.toanyone.store.domain.repository.StoreRepository;
import com.toanyone.store.domain.service.StoreService;
import com.toanyone.store.infrastructure.client.HubClient;
import com.toanyone.store.presentation.dto.SingleResponse;
import com.toanyone.store.presentation.dto.StoreCreateRequestDto;
import com.toanyone.store.presentation.dto.StoreCreateResponseDto;
import com.toanyone.store.presentation.dto.StoreFindResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import com.toanyone.store.infrastructure.client.dto.HubResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@Transactional
class StoreServiceImplTest {

    @Autowired
    StoreRepository storeRepository;

    @Autowired  // ✅ 변경: @InjectMocks → @Autowired
    private StoreService storeService;

    @MockBean  // ✅ 변경: @Mock 대신 @MockBean 사용
    private HubClient hubClient;


    @Test
    @DisplayName("업체 생성 테스트")
    void StoreCreateTest() {
        //given
        HubResponseDto mockHubResponse = new HubResponseDto(1L, "허브 A", new DetailAddress("a동 102동"),
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)));
        when(hubClient.getHubById(1L)).thenReturn(SingleResponse.success(mockHubResponse));
        
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
    void storeFindOneTest(){
        // given
        HubResponseDto mockHubResponse = new HubResponseDto(1L, "허브 A", new DetailAddress("a동 102동"),
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)));
        when(hubClient.getHubById(1L)).thenReturn(SingleResponse.success(mockHubResponse));

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
    void storeDeleteTest(){
        // given
        Store savedStore = storeRepository.save(Store.create("찬이네 서울 정육점", 1L, new DetailAddress("102동 1201호"),
                new Location(new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP), new BigDecimal("123.1234567").setScale(7, RoundingMode.HALF_UP)),
                StoreType.PRODUCER, "02-123-4567"));

        // when
        savedStore.delete(savedStore.getId());
        Store findStore = storeRepository.findById(savedStore.getId()).get();

        // then
        assertThat(findStore.getDeletedAt()).isNotNull();
    }
}