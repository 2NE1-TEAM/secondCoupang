package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dtos.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.exception.DeliveryManagerException;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryManager.DeliveryManagerType;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
}