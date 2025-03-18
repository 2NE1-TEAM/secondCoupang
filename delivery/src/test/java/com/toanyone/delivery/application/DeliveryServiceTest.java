package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dtos.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dtos.request.GetDeliveryManagerSearchConditionRequestDto;
import com.toanyone.delivery.application.dtos.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.application.exception.DeliveryManagerException;
import com.toanyone.delivery.common.utils.MultiResponse.CursorInfo;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryManager.DeliveryManagerType;
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @InjectMocks
    private DeliveryService deliveryService;
    @Mock
    private DeliveryManagerRepository deliveryManagerRepository;
    @Mock
    private CustomDeliveryMangerRepository customDeliveryMangerRepository;

    @Test
    @DisplayName("배송담닫자 생성 테스트")
    public void createDeliveryManagerTest() {

        // given
        CreateDeliveryManagerRequestDto request = CreateDeliveryManagerRequestDto.builder()
                .deliveryManagerType("허브 배송 담당자")
                .deliveryOrder(1L)
                .hubId(1L)
                .userId(1L)
                .build();

        DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(request.getUserId(),
                DeliveryManagerType.fromValue(request.getDeliveryManagerType()).get(),
                request.getHubId(), request.getDeliveryOrder());
        ReflectionTestUtils.setField(deliveryManager, "id", 1L);
        when(deliveryManagerRepository.save(any(DeliveryManager.class))).thenReturn(deliveryManager);

        // when
        Long deliveryManagerId = deliveryService.createDeliveryManager(request);

        // then
        assertNotNull(deliveryManagerId);
        assertEquals(deliveryManagerId, deliveryManager.getId());

    }

    @Test
    @DisplayName("배송담닫자 생성 실패 테스트")
    public void createDeliveryManagerFailedTest() {

        // given
        CreateDeliveryManagerRequestDto request = CreateDeliveryManagerRequestDto.builder()
                .deliveryManagerType("가짜 배송 담당자")
                .deliveryOrder(1L)
                .hubId(1L)
                .userId(1L)
                .build();

        // when - then
        Assertions.assertThatThrownBy(() -> deliveryService.createDeliveryManager(request))
                .isInstanceOf(DeliveryManagerException.InvalidDeliveryManagerTypeException.class);

    }

    @Test
    @DisplayName("배송담당자 단건 조회 테스트")
    public void findDeliveryManagerTest() {

        // given
        Long deliveryManagerId = 1L;
        DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(deliveryManagerId, DeliveryManagerType.HUB_DELIVERY_MANAGER, 1L, 1L);
        ReflectionTestUtils.setField(deliveryManager, "id", 1L);

        // when
        when(deliveryManagerRepository.findById(deliveryManagerId)).thenReturn(Optional.of(deliveryManager));

        // then
        GetDeliveryManagerResponseDto response = deliveryService.getDeliveryManager(deliveryManagerId);
        assertNotNull(response);
        assertEquals(deliveryManagerId, response.getDeliveryManagerId());
    }

    @Test
    @DisplayName("배송담당자 단건 조회 실패 테스트")
    public void findDeliveryManagerFailedTest() {

        // given
        Long notExistsDeliveryManagerId = 2L;
        DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.HUB_DELIVERY_MANAGER, 1L, 1L);
        ReflectionTestUtils.setField(deliveryManager, "id", 1L);

        // when
        when(deliveryManagerRepository.findById(notExistsDeliveryManagerId))
                .thenReturn(Optional.empty());

        // then
        Assertions.assertThatThrownBy(() -> deliveryService.getDeliveryManager(notExistsDeliveryManagerId))
                .isInstanceOf(DeliveryManagerException.NotFoundManagerException.class);

    }

    @Test
    @DisplayName("배송담당자 다건 조회 테스트")
    public void getDeliveryManagersTest() {

        // given
        GetDeliveryManagerSearchConditionRequestDto request = GetDeliveryManagerSearchConditionRequestDto.builder()
                .deliveryManagerType("허브 배송 담당자")
                .sortBy("오름차순")
                .deliveryManagerId(1L)
                .limit(10)
                .build();

        DeliveryManager deliveryManager1 = DeliveryManager.createDeliveryManager(2L, DeliveryManagerType.fromValue("허브 배송 담당자").get(), 1L, 2L);
        DeliveryManager deliveryManager2 = DeliveryManager.createDeliveryManager(3L, DeliveryManagerType.fromValue("허브 배송 담당자").get(), 1L, 3L);

        ReflectionTestUtils.setField(deliveryManager1, "id", 2L);
        ReflectionTestUtils.setField(deliveryManager2, "id", 3L);


        List<GetDeliveryManagerResponseDto> responseDtos = List.of(GetDeliveryManagerResponseDto.from(deliveryManager1), GetDeliveryManagerResponseDto.from(deliveryManager2));
        CursorPage<GetDeliveryManagerResponseDto> cursorPage = new CursorPage<>(responseDtos,
                new CursorInfo(responseDtos.get(responseDtos.size() - 1).getDeliveryManagerId()),
                true);

        // when
        when(customDeliveryMangerRepository
                .getDeliveryManagers(request.getDeliveryManagerId(), request.getSortBy(),
                        DeliveryManagerType.fromValue(request.getDeliveryManagerType()).get(), request.getLimit()))
                .thenReturn(cursorPage);

        // then

        CursorPage<GetDeliveryManagerResponseDto> deliveryManagers = deliveryService.getDeliveryManagers(request);

        assertNotNull(deliveryManagers);
        assertEquals(2, deliveryManagers.getContent().size());
        assertThat(deliveryManagers.getContent().stream().map(GetDeliveryManagerResponseDto::getDeliveryManagerId)
                .anyMatch(id -> id.equals(2L))).isTrue();
        assertThat(deliveryManagers.getContent().stream().map(GetDeliveryManagerResponseDto::getDeliveryManagerId)
                .anyMatch(id -> id.equals(3L))).isTrue();

    }

    @Test
    @DisplayName("배송 담당자 다건 조회 실패 테스트")
    public void getDeliveryManagersFailedTest() {

        // given
        GetDeliveryManagerSearchConditionRequestDto request = GetDeliveryManagerSearchConditionRequestDto.builder()
                .deliveryManagerType("가짜 배송 담당자")
                .deliveryManagerId(1L)
                .sortBy("내림차순")
                .limit(10)
                .build();
        
        // when - then
        Assertions.assertThatThrownBy(() -> deliveryService.getDeliveryManagers(request))
                .isInstanceOf(DeliveryManagerException.InvalidDeliveryManagerTypeException.class);

    }
}