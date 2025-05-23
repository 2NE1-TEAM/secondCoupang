package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dto.request.CreateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dto.request.GetDeliveryManagerSearchConditionRequestDto;
import com.toanyone.delivery.application.dto.request.UpdateDeliveryManagerRequestDto;
import com.toanyone.delivery.application.dto.response.DeleteDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dto.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dto.response.UpdateDeliveryManagerResponseDto;
import com.toanyone.delivery.application.exception.DeliveryManagerException;
import com.toanyone.delivery.common.utils.MultiResponse;
import com.toanyone.delivery.common.utils.SingleResponse;
import com.toanyone.delivery.common.utils.UserContext;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import com.toanyone.delivery.infrastructure.client.HubClient;
import com.toanyone.delivery.infrastructure.client.dto.HubFindResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryManagerServiceTest {
    @InjectMocks
    private DeliveryManagerService deliveryManagerService;
    @Mock
    private DeliveryManagerRepository deliveryManagerRepository;
    @Mock
    private CustomDeliveryMangerRepository customDeliveryMangerRepository;
    @Mock
    private HubClient hubClient;

    @Nested
    class DeliveryManagerTest {
        @Test
        @DisplayName("업체 배송담당자 생성 테스트")
        public void createStoreDeliveryManagerTest() {

            // given
            CreateDeliveryManagerRequestDto request = CreateDeliveryManagerRequestDto.builder()
                    .deliveryManagerType("업체 배송 담당자")
                    .hubId(1L)
                    .userId(1L)
                    .name("익명")
                    .build();

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(request.getUserId(),
                    DeliveryManager.DeliveryManagerType.fromValue(request.getDeliveryManagerType()).get(),
                    request.getHubId(), 1L, request.getName());
            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            when(hubClient.getHubById(request.getHubId())).thenReturn(ResponseEntity.ok(SingleResponse.success(new HubFindResponseDto())));
            when(deliveryManagerRepository.save(any(DeliveryManager.class))).thenReturn(deliveryManager);

            // when
            Long deliveryManagerId = deliveryManagerService.createDeliveryManager(request);

            // then
            assertNotNull(deliveryManagerId);
            assertEquals(deliveryManagerId, deliveryManager.getId());

        }

        @Test
        @DisplayName("허브 배송담당자 생성 테스트")
        public void createHubDeliveryManagerTest() {

            // given
            CreateDeliveryManagerRequestDto request = CreateDeliveryManagerRequestDto.builder()
                    .deliveryManagerType("허브 배송 담당자")
                    .hubId(0L)
                    .userId(1L)
                    .name("익명")

                    .build();

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(request.getUserId(),
                    DeliveryManager.DeliveryManagerType.fromValue(request.getDeliveryManagerType()).get(),
                    request.getHubId(), 1L, request.getName());
            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            when(deliveryManagerRepository.save(any(DeliveryManager.class))).thenReturn(deliveryManager);

            // when
            Long deliveryManagerId = deliveryManagerService.createDeliveryManager(request);

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
                    .hubId(1L)
                    .userId(1L)
                    .build();

            // when - then
            Assertions.assertThatThrownBy(() -> deliveryManagerService.createDeliveryManager(request))
                    .isInstanceOf(DeliveryManagerException.InvalidDeliveryManagerTypeException.class);

        }

        @Test
        @DisplayName("배송담당자 단건 조회 테스트")
        public void findDeliveryManagerTest() {

            // given
            Long deliveryManagerId = 1L;
            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(deliveryManagerId, DeliveryManager.DeliveryManagerType.HUB_DELIVERY_MANAGER, 1L, 1L, "사용자1");
            ReflectionTestUtils.setField(deliveryManager, "id", 1L);

            // when
            when(deliveryManagerRepository.findById(deliveryManagerId)).thenReturn(Optional.of(deliveryManager));

            // then
            GetDeliveryManagerResponseDto response = deliveryManagerService.getDeliveryManager(deliveryManagerId);
            assertNotNull(response);
            assertEquals(deliveryManagerId, response.getDeliveryManagerId());
        }

        @Test
        @DisplayName("배송담당자 단건 조회 실패 테스트")
        public void findDeliveryManagerFailedTest() {

            // given
            Long notExistsDeliveryManagerId = 2L;
            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.HUB_DELIVERY_MANAGER, 1L, 1L, "사용자1");
            ReflectionTestUtils.setField(deliveryManager, "id", 1L);

            // when
            when(deliveryManagerRepository.findById(notExistsDeliveryManagerId))
                    .thenReturn(Optional.empty());

            // then
            Assertions.assertThatThrownBy(() -> deliveryManagerService.getDeliveryManager(notExistsDeliveryManagerId))
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
                    .userId(1L)
                    .name("익명")
                    .limit(10)
                    .build();

            DeliveryManager deliveryManager1 = DeliveryManager.createDeliveryManager(2L, DeliveryManager.DeliveryManagerType.fromValue("허브 배송 담당자").get(), 1L, 2L, "사원1");
            DeliveryManager deliveryManager2 = DeliveryManager.createDeliveryManager(3L, DeliveryManager.DeliveryManagerType.fromValue("허브 배송 담당자").get(), 1L, 3L, "사원2");

            ReflectionTestUtils.setField(deliveryManager1, "id", 2L);
            ReflectionTestUtils.setField(deliveryManager2, "id", 3L);


            List<GetDeliveryManagerResponseDto> responseDtos = List.of(GetDeliveryManagerResponseDto.from(deliveryManager1), GetDeliveryManagerResponseDto.from(deliveryManager2));
            long cursorId = responseDtos.stream().mapToLong(GetDeliveryManagerResponseDto::getDeliveryManagerId)
                    .max().getAsLong();

            MultiResponse.CursorPage<GetDeliveryManagerResponseDto> cursorPage = new MultiResponse.CursorPage<>(responseDtos,
                    new MultiResponse.CursorInfo(cursorId),
                    true);

            // when
            when(customDeliveryMangerRepository
                    .getDeliveryManagers(request.getDeliveryManagerId(), request.getSortBy(),
                            DeliveryManager.DeliveryManagerType.fromValue(request.getDeliveryManagerType()).get(), request.getUserId(), request.getName(), request.getLimit()))
                    .thenReturn(cursorPage);

            // then

            MultiResponse.CursorPage<GetDeliveryManagerResponseDto> deliveryManagers = deliveryManagerService.getDeliveryManagers(request);

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
            Assertions.assertThatThrownBy(() -> deliveryManagerService.getDeliveryManagers(request))
                    .isInstanceOf(DeliveryManagerException.InvalidDeliveryManagerTypeException.class);
        }

        @Test
        @DisplayName("배송 담당자 수정 테스트 - 마스터 관리자")
        public void updateDeliveryManagerByMasterTest() {

            // given
            UpdateDeliveryManagerRequestDto request = UpdateDeliveryManagerRequestDto.builder()
                    .name("수정한 사람")
                    .build();

            Long toBeUpdatedDeliveryManagerId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("MASTER")
                    .build());

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정전 이름");

            DeliveryManager updatedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(updatedDeliveryManager, "id", 1L);

            // when
            when(deliveryManagerRepository.findById(1L)).thenReturn(Optional.of(deliveryManager));
            when(deliveryManagerRepository.save(deliveryManager)).thenReturn(updatedDeliveryManager);

            // then
            UpdateDeliveryManagerResponseDto updateDeliveryManagerResponseDto = deliveryManagerService.updateDeliveryManager(toBeUpdatedDeliveryManagerId, request);
            assertNotNull(updateDeliveryManagerResponseDto);
            assertThat(updateDeliveryManagerResponseDto.getDeliveryManagerId()).isEqualTo(toBeUpdatedDeliveryManagerId);
            assertThat(updateDeliveryManagerResponseDto.getName()).isEqualTo(request.getName());

        }

        @Test
        @DisplayName("배송 담당자 수정 테스트 - 담당허브 관리자")
        public void updateDeliveryManagerByHubManagerTest() {

            // given
            UpdateDeliveryManagerRequestDto request = UpdateDeliveryManagerRequestDto.builder()
                    .name("수정한 사람")
                    .build();

            Long toBeUpdatedDeliveryManagerId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("HUB")
                    .hubId(1L)
                    .build());

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정전 이름");

            DeliveryManager updatedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(updatedDeliveryManager, "id", 1L);

            // when
            when(deliveryManagerRepository.findById(1L)).thenReturn(Optional.of(deliveryManager));
            when(deliveryManagerRepository.save(deliveryManager)).thenReturn(updatedDeliveryManager);

            // then
            UpdateDeliveryManagerResponseDto updateDeliveryManagerResponseDto = deliveryManagerService.updateDeliveryManager(toBeUpdatedDeliveryManagerId, request);
            assertNotNull(updateDeliveryManagerResponseDto);
            assertThat(updateDeliveryManagerResponseDto.getDeliveryManagerId()).isEqualTo(toBeUpdatedDeliveryManagerId);
            assertThat(updateDeliveryManagerResponseDto.getName()).isEqualTo(request.getName());

        }

        @Test
        @DisplayName("배송 담당자 수정 테스트 - 권한 없는 유저")
        public void updateDeliveryManagerByUnAuthorizedUserTest() {

            // given
            UpdateDeliveryManagerRequestDto request = UpdateDeliveryManagerRequestDto.builder()
                    .name("수정한 사람")
                    .build();

            Long toBeUpdatedDeliveryManagerId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("HUB")
                    .hubId(2L)
                    .build());

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정전 이름");

            DeliveryManager updatedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(updatedDeliveryManager, "id", 1L);

            // when
            when(deliveryManagerRepository.findById(1L)).thenReturn(Optional.of(deliveryManager));

            // then
            Assertions.assertThatThrownBy(() -> deliveryManagerService.updateDeliveryManager(toBeUpdatedDeliveryManagerId, request))
                    .isInstanceOf(DeliveryManagerException.UnauthorizedDeliveryManagerEditException.class);
        }

        @Test
        @DisplayName("배송 담당자 삭제 테스트 - 마스터 관리자")
        public void deleteDeliveryManagerByMasterTest() {

            // given
            Long toBeDeletedDeliveryManagerId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("MASTER")
                    .hubId(1L)
                    .build());

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "이름");

            DeliveryManager deletedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deletedDeliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deliveryManager, "deletedBy", UserContext.getUserContext().getUserId());

            // when
            when(deliveryManagerRepository.findById(toBeDeletedDeliveryManagerId)).thenReturn(Optional.of(deliveryManager));
            when(deliveryManagerRepository.save(deliveryManager)).thenReturn(deletedDeliveryManager);

            // then
            DeleteDeliveryManagerResponseDto response = deliveryManagerService.deleteDeliveryManager(toBeDeletedDeliveryManagerId);
            assertNotNull(response);
            assertThat(response.getDeliveryManagerId()).isEqualTo(toBeDeletedDeliveryManagerId);

        }

        @Test
        @DisplayName("배송 담당자 삭제 테스트 - 담당 허브 관리자")
        public void deleteDeliveryManagerByHubManagerTest() {

            // given
            Long toBeDeletedDeliveryManagerId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("HUB")
                    .hubId(1L)
                    .build());

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "이름");

            DeliveryManager deletedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deletedDeliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deliveryManager, "deletedBy", UserContext.getUserContext().getUserId());

            // when
            when(deliveryManagerRepository.findById(toBeDeletedDeliveryManagerId)).thenReturn(Optional.of(deliveryManager));
            when(deliveryManagerRepository.save(deliveryManager)).thenReturn(deletedDeliveryManager);

            // then
            DeleteDeliveryManagerResponseDto response = deliveryManagerService.deleteDeliveryManager(toBeDeletedDeliveryManagerId);
            assertNotNull(response);
            assertThat(response.getDeliveryManagerId()).isEqualTo(toBeDeletedDeliveryManagerId);

        }

        @Test
        @DisplayName("배송 담당자 삭제 테스트 - 권한 없는 유저")
        public void deleteDeliveryManagerByUnauthorizedUserTest() {

            // given
            Long toBeDeletedDeliveryManagerId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("DELIVERY")
                    .hubId(1L)
                    .build());

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "이름");

            DeliveryManager deletedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManager.DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deletedDeliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deliveryManager, "deletedBy", UserContext.getUserContext().getUserId());

            // when
            when(deliveryManagerRepository.findById(toBeDeletedDeliveryManagerId)).thenReturn(Optional.of(deliveryManager));

            // then
            Assertions.assertThatThrownBy(() -> deliveryManagerService.deleteDeliveryManager(toBeDeletedDeliveryManagerId))
                    .isInstanceOf(DeliveryManagerException.UnauthorizedDeliveryManagerDeleteException.class);

        }
    }
}