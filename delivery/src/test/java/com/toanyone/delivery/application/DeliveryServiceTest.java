package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dtos.request.*;
import com.toanyone.delivery.application.dtos.response.*;
import com.toanyone.delivery.application.exception.DeliveryException;
import com.toanyone.delivery.application.exception.DeliveryManagerException;
import com.toanyone.delivery.common.utils.MultiResponse.CursorInfo;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.common.utils.SingleResponse;
import com.toanyone.delivery.common.utils.UserContext;
import com.toanyone.delivery.domain.Delivery;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryManager.DeliveryManagerType;
import com.toanyone.delivery.domain.DeliveryRoad;
import com.toanyone.delivery.domain.repository.CustomDeliveryMangerRepository;
import com.toanyone.delivery.domain.repository.CustomDeliveryRepository;
import com.toanyone.delivery.domain.repository.DeliveryManagerRepository;
import com.toanyone.delivery.domain.repository.DeliveryRepository;
import com.toanyone.delivery.infrastructure.client.AiClient;
import com.toanyone.delivery.infrastructure.client.HubClient;
import com.toanyone.delivery.infrastructure.client.dto.HubFindResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @InjectMocks
    private DeliveryService deliveryService;
    @Mock
    private HubClient hubClient;
    @Mock
    private AiClient aiClient;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private DeliveryManagerRepository deliveryManagerRepository;
    @Mock
    private CustomDeliveryMangerRepository customDeliveryMangerRepository;
    @Mock
    private CustomDeliveryRepository customDeliveryRepository;
    @Captor
    private ArgumentCaptor<Message<DeliveryCompletedMessage>> messageCaptor;

    @Nested
    class DeliveryTest {

        @Test
        @DisplayName("배송 생성 테스트")
        void createDeliveryTest() {
        }


        @Test
        @DisplayName("배송 단건 조회 테스트")
        void getDeliveryTest() {

            // given
            Long deliveryId = 1L;

            List<DeliveryRoad> deliveryRoads = List.of(DeliveryRoad.createDeliveryRoad(1L, 1, 1L, 2L, BigDecimal.valueOf(150), 50),
                    DeliveryRoad.createDeliveryRoad(2L, 2, 2L, 3L, BigDecimal.valueOf(100), 30));

            Delivery delivery = Delivery.createDelivery(1L, deliveryRoads, 1L, 3L, "예시주소", "예시수령인", "예시아이디", 3L);
            ReflectionTestUtils.setField(delivery, "id", 1L);

            // when
            when(deliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

            // then
            GetDeliveryResponseDto response = deliveryService.getDelivery(deliveryId);
            assertNotNull(response);
            assertEquals(deliveryId, response.getDeliveryId());

        }

        @Test
        @DisplayName("배송 단건 조회 실패 테스트")
        void getDeliveryFailedTest() {

            // given
            Long notExistsDeliveryId = 1L;

            List<DeliveryRoad> deliveryRoads = List.of(DeliveryRoad.createDeliveryRoad(1L, 1, 1L, 2L, BigDecimal.valueOf(150), 50),
                    DeliveryRoad.createDeliveryRoad(2L, 2, 2L, 3L, BigDecimal.valueOf(100), 30));

            Delivery delivery = Delivery.createDelivery(1L, deliveryRoads, 1L, 3L, "예시주소", "예시수령인", "예시아이디", 3L);
            ReflectionTestUtils.setField(delivery, "id", 1L);

            // when
            when(deliveryRepository.findById(notExistsDeliveryId)).thenReturn(Optional.empty());

            // then
            Assertions.assertThatThrownBy(() -> deliveryService.getDelivery(notExistsDeliveryId))
                    .isInstanceOf(DeliveryException.DeliveryNotFoundException.class);

        }

        @Test
        @DisplayName("배송 다건 조회 테스트")
        void getDeliveriesTest() {

            // given
            Long deliveryId = 1L;

            List<DeliveryRoad> deliveryRoads1 = List.of(
                    DeliveryRoad.createDeliveryRoad(1L, 1, 1L, 2L, BigDecimal.valueOf(150), 50),
                    DeliveryRoad.createDeliveryRoad(2L, 2, 2L, 3L, BigDecimal.valueOf(100), 30)
            );

            Delivery delivery1 = Delivery.createDelivery(1L, deliveryRoads1, 1L, 3L, "서울시 강남구", "홍길동", "예시아이디", 3L);
            ReflectionTestUtils.setField(delivery1, "id", 2L);

            List<DeliveryRoad> deliveryRoads2 = List.of(
                    DeliveryRoad.createDeliveryRoad(3L, 1, 3L, 4L, BigDecimal.valueOf(200), 60),
                    DeliveryRoad.createDeliveryRoad(4L, 2, 4L, 5L, BigDecimal.valueOf(120), 40)
            );

            Delivery delivery2 = Delivery.createDelivery(2L, deliveryRoads2, 3L, 5L, "부산시 해운대구", "김철수", "예시아이디", 5L);
            ReflectionTestUtils.setField(delivery2, "id", 3L);

            GetDeliverySearchConditionRequestDto request = GetDeliverySearchConditionRequestDto.builder()
                    .deliveryId(1L)
                    .deliveryStatus("허브 이동 대기중")
                    .departureHubId(1L)
                    .arrivalHubId(2L)
                    .recipient("수령인")
                    .storeDeliveryManagerId(1L)
                    .limit(10)
                    .sortBy("오름차순")
                    .build();

            Delivery.DeliveryStatus deliveryStatus = Delivery.DeliveryStatus.fromValue(request.getDeliveryStatus()).get();


            List<GetDeliveryResponseDto> responseDtos = List.of(GetDeliveryResponseDto.from(delivery1), GetDeliveryResponseDto.from(delivery2)
            );

            long cursorId = responseDtos.stream().mapToLong(GetDeliveryResponseDto::getDeliveryId)
                    .max()
                    .getAsLong();

            CursorPage<GetDeliveryResponseDto> cursorPage = new CursorPage<>(responseDtos, new CursorInfo(cursorId), true);

            // when
            when(customDeliveryRepository.getDeliveries(request.getDeliveryId(), deliveryStatus, request.getDepartureHubId(),
                    request.getArrivalHubId(), request.getRecipient(), request.getStoreDeliveryManagerId(), request.getLimit(), request.getSortBy()))
                    .thenReturn(cursorPage);

            // then
            CursorPage<GetDeliveryResponseDto> response = deliveryService.getDeliveries(request);

            assertNotNull(response);
            assertEquals(2, response.getContent().size());
            assertThat(response.getContent().stream().map(GetDeliveryResponseDto::getDeliveryId)
                    .anyMatch(id -> id.equals(2L)))
                    .isTrue();
            assertThat(response.getContent().stream().map(GetDeliveryResponseDto::getDeliveryId)
                    .anyMatch(id -> id.equals(3L)))
                    .isTrue();

        }

        @Test
        @DisplayName("배송 정보 삭제 테스트 - 마스터 관리자")
        public void deleteDeliveryByMaster() {

            // given
            Long toBeDeletedDeliveryId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("MASTER")
                    .hubId(1L)
                    .build());

            Delivery delivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L,
                    "예시주소", "예시수령인", "ex", 1L);

            Delivery deletedDelivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L,
                    "예시주소", "예시수령인", "ex", 1L);


            ReflectionTestUtils.setField(delivery, "id", toBeDeletedDeliveryId);
            ReflectionTestUtils.setField(deletedDelivery, "id", toBeDeletedDeliveryId);
            ReflectionTestUtils.setField(deletedDelivery, "deletedBy", UserContext.getUserContext().getUserId());

            // when
            when(deliveryRepository.findById(toBeDeletedDeliveryId)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(delivery)).thenReturn(deletedDelivery);

            // then
            DeleteDeliveryResponseDto response = deliveryService.deleteDelivery(toBeDeletedDeliveryId);
            assertNotNull(response);
            assertThat(response.getDeletedDeliveryId()).isEqualTo(toBeDeletedDeliveryId);


        }

        @Test
        @DisplayName("배송 정보 삭제 테스트 - 담당 허브 관리자")
        public void deleteDeliveryByHubManager() {

            // given
            Long toBeDeletedDeliveryId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("HUB")
                    .hubId(1L)
                    .build());

            Delivery delivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L,
                    "예시주소", "예시수령인", "ex", 1L);

            Delivery deletedDelivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L,
                    "예시주소", "예시수령인", "ex", 1L);


            ReflectionTestUtils.setField(delivery, "id", toBeDeletedDeliveryId);
            ReflectionTestUtils.setField(deletedDelivery, "id", toBeDeletedDeliveryId);
            ReflectionTestUtils.setField(deletedDelivery, "deletedBy", UserContext.getUserContext().getUserId());

            // when
            when(deliveryRepository.findById(toBeDeletedDeliveryId)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(delivery)).thenReturn(deletedDelivery);

            // then
            DeleteDeliveryResponseDto response = deliveryService.deleteDelivery(toBeDeletedDeliveryId);
            assertNotNull(response);
            assertThat(response.getDeletedDeliveryId()).isEqualTo(toBeDeletedDeliveryId);


        }

        @Test
        @DisplayName("배송 정보 삭제 테스트 - 권한 없는 유저")
        public void deleteDeliveryByUnauthorizedUser() {

            // given
            Long toBeDeletedDeliveryId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("DELIVERY")
                    .hubId(1L)
                    .build());

            Delivery delivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L,
                    "예시주소", "예시수령인", "ex", 1L);


            ReflectionTestUtils.setField(delivery, "id", toBeDeletedDeliveryId);


            // when
            when(deliveryRepository.findById(toBeDeletedDeliveryId)).thenReturn(Optional.of(delivery));

            // then
            Assertions.assertThatThrownBy(() -> deliveryService.deleteDelivery(toBeDeletedDeliveryId))
                    .isInstanceOf(DeliveryException.UnauthorizedDeliveryDeleteException.class);
        }

        @Test
        @DisplayName("배송 수정 테스트(배송 완료에 의한 메시지 발송) - 관리자")
        public void updateDeliveryByMaster() {

            // given
            UpdateDeliveryRequestDto request = UpdateDeliveryRequestDto.builder()
                    .deliveryAddress("수정주소")
                    .deliveryStatus("배송완료")
                    .recipient("수정수령인")
                    .recipientSlackId("venus")
                    .build();

            Long toBeUpdatedDeliveryId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("MASTER")
                    .build());

            Delivery delivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L, "수정전주소", "수정전수령인", "before", 1L);
            Delivery updatedDelivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L, "수정주소", "수정수령인", "venus", 1L);

            ReflectionTestUtils.setField(delivery, "id", toBeUpdatedDeliveryId);
            ReflectionTestUtils.setField(updatedDelivery, "id", toBeUpdatedDeliveryId);

            ReflectionTestUtils.setField(delivery, "deliveryStatus", Delivery.DeliveryStatus.DESTINATION_HUB_ARRIVED);
            ReflectionTestUtils.setField(updatedDelivery, "deliveryStatus", Delivery.DeliveryStatus.DELIVERY_COMPLETED);

            DeliveryCompletedMessage message = DeliveryCompletedMessage.builder()
                    .completedDeliveryId(toBeUpdatedDeliveryId)
                    .message("배송완료")
                    .build();

            Message<DeliveryCompletedMessage> kafkaMesage = MessageBuilder.withPayload(message)
                    .setHeader(KafkaHeaders.TOPIC, "delivery.completed")
                    .build();

            //when
            when(deliveryRepository.findById(toBeUpdatedDeliveryId)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(delivery)).thenReturn(updatedDelivery);
            when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

            // then
            UpdateDeliveryResponseDto response = deliveryService.updateDelivery(toBeUpdatedDeliveryId, request);
            assertNotNull(response);
            assertThat(response.getUpdatedDeliveryId()).isEqualTo(toBeUpdatedDeliveryId);
            verify(kafkaTemplate).send(messageCaptor.capture());
            Message<DeliveryCompletedMessage> capturedMessage = messageCaptor.getValue();
            assertNotNull(capturedMessage);

        }

        @Test
        @DisplayName("배송 수정 테스트(배송 완료에 의한 메시지 발송) - 담당 허브 관리자")
        public void updateDeliveryByHubManager() {

            // given
            UpdateDeliveryRequestDto request = UpdateDeliveryRequestDto.builder()
                    .deliveryAddress("수정주소")
                    .deliveryStatus("배송완료")
                    .recipient("수정수령인")
                    .recipientSlackId("venus")
                    .build();

            Long toBeUpdatedDeliveryId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("HUB")
                    .hubId(1L)
                    .build());

            Delivery delivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L, "수정전주소", "수정전수령인", "before", 1L);
            Delivery updatedDelivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L, "수정주소", "수정수령인", "venus", 1L);

            ReflectionTestUtils.setField(delivery, "id", toBeUpdatedDeliveryId);
            ReflectionTestUtils.setField(updatedDelivery, "id", toBeUpdatedDeliveryId);

            ReflectionTestUtils.setField(delivery, "deliveryStatus", Delivery.DeliveryStatus.DESTINATION_HUB_ARRIVED);
            ReflectionTestUtils.setField(updatedDelivery, "deliveryStatus", Delivery.DeliveryStatus.DELIVERY_COMPLETED);

            DeliveryCompletedMessage message = DeliveryCompletedMessage.builder()
                    .completedDeliveryId(toBeUpdatedDeliveryId)
                    .message("배송완료")
                    .build();

            Message<DeliveryCompletedMessage> kafkaMessage = MessageBuilder.withPayload(message)
                    .setHeader(KafkaHeaders.TOPIC, "delivery.completed")
                    .build();

            //when
            when(deliveryRepository.findById(toBeUpdatedDeliveryId)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(delivery)).thenReturn(updatedDelivery);
            when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

            // then
            UpdateDeliveryResponseDto response = deliveryService.updateDelivery(toBeUpdatedDeliveryId, request);
            assertNotNull(response);
            assertThat(response.getUpdatedDeliveryId()).isEqualTo(toBeUpdatedDeliveryId);
            verify(kafkaTemplate).send(messageCaptor.capture());
            Message<DeliveryCompletedMessage> capturedMessage = messageCaptor.getValue();
            assertNotNull(capturedMessage);

        }

        @Test
        @DisplayName("배송 수정 테스트(배송 완료에 의한 메시지 발송) - 업체 배송 담당자")
        public void updateDeliveryByStoreManager() {

            // given
            UpdateDeliveryRequestDto request = UpdateDeliveryRequestDto.builder()
                    .deliveryAddress("수정주소")
                    .deliveryStatus("배송완료")
                    .recipient("수정수령인")
                    .recipientSlackId("venus")
                    .build();

            Long toBeUpdatedDeliveryId = 1L;

            Long storeDeliveryManagerId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("DELIVERY")
                    .hubId(0L)
                    .userId(1L)
                    .build());


            Delivery delivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L, "수정전주소", "수정전수령인", "before", 1L);
            Delivery updatedDelivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L, "수정주소", "수정수령인", "venus", 1L);

            ReflectionTestUtils.setField(delivery, "id", toBeUpdatedDeliveryId);
            ReflectionTestUtils.setField(updatedDelivery, "id", toBeUpdatedDeliveryId);

            ReflectionTestUtils.setField(delivery, "deliveryStatus", Delivery.DeliveryStatus.DESTINATION_HUB_ARRIVED);
            ReflectionTestUtils.setField(updatedDelivery, "deliveryStatus", Delivery.DeliveryStatus.DELIVERY_COMPLETED);

            DeliveryManager storeDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 0L, 1L, "업체");

            DeliveryCompletedMessage message = DeliveryCompletedMessage.builder()
                    .completedDeliveryId(toBeUpdatedDeliveryId)
                    .message("배송완료")
                    .build();

            Message<DeliveryCompletedMessage> kafkaMesage = MessageBuilder.withPayload(message)
                    .setHeader(KafkaHeaders.TOPIC, "delivery.completed")
                    .build();

            //when
            when(deliveryRepository.findById(toBeUpdatedDeliveryId)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(delivery)).thenReturn(updatedDelivery);
            when(deliveryManagerRepository.findById(storeDeliveryManagerId)).thenReturn(Optional.of(storeDeliveryManager));
            when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

            // then
            UpdateDeliveryResponseDto response = deliveryService.updateDelivery(toBeUpdatedDeliveryId, request);
            assertNotNull(response);
            assertThat(response.getUpdatedDeliveryId()).isEqualTo(toBeUpdatedDeliveryId);
            verify(kafkaTemplate).send(messageCaptor.capture());
            Message<DeliveryCompletedMessage> capturedMessage = messageCaptor.getValue();
            assertNotNull(capturedMessage);

        }

        @Test
        @DisplayName("배송 수정 테스트(배송 완료에 의한 메시지 발송) - 권한 없는 유저")
        public void updateDeliveryByUnauthorizedUser() {

            // given
            UpdateDeliveryRequestDto request = UpdateDeliveryRequestDto.builder()
                    .deliveryAddress("수정주소")
                    .deliveryStatus("배송완료")
                    .recipient("수정수령인")
                    .recipientSlackId("venus")
                    .build();

            Long toBeUpdatedDeliveryId = 1L;

            Long storeDeliveryManagerId = 1L;

            UserContext.setCurrentContext(UserContext.builder()
                    .role("STORE")
                    .hubId(1L)
                    .userId(1L)
                    .build());


            Delivery delivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L, "수정전주소", "수정전수령인", "before", 1L);
            Delivery updatedDelivery = Delivery.createDelivery(1L, Collections.emptyList(), 1L, 2L, "수정주소", "수정수령인", "venus", 1L);

            ReflectionTestUtils.setField(delivery, "id", toBeUpdatedDeliveryId);
            ReflectionTestUtils.setField(updatedDelivery, "id", toBeUpdatedDeliveryId);

            ReflectionTestUtils.setField(delivery, "deliveryStatus", Delivery.DeliveryStatus.DESTINATION_HUB_ARRIVED);
            ReflectionTestUtils.setField(updatedDelivery, "deliveryStatus", Delivery.DeliveryStatus.DELIVERY_COMPLETED);

            DeliveryManager storeDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 0L, 1L, "업체");

            DeliveryCompletedMessage message = DeliveryCompletedMessage.builder()
                    .completedDeliveryId(toBeUpdatedDeliveryId)
                    .message("배송완료")
                    .build();

            Message<DeliveryCompletedMessage> kafkaMesage = MessageBuilder.withPayload(message)
                    .setHeader(KafkaHeaders.TOPIC, "delivery.completed")
                    .build();

            // when
            when(deliveryRepository.findById(toBeUpdatedDeliveryId)).thenReturn(Optional.of(delivery));

            // then
            assertThatThrownBy(() -> deliveryService.updateDelivery(toBeUpdatedDeliveryId, request))
                    .isInstanceOf(DeliveryException.UnauthorizedDeliveryUpdateException.class);

        }
    }


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
                    DeliveryManagerType.fromValue(request.getDeliveryManagerType()).get(),
                    request.getHubId(), 1L, request.getName());
            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            when(hubClient.getHubById(request.getHubId())).thenReturn(ResponseEntity.ok(SingleResponse.success(new HubFindResponseDto())));
            when(deliveryManagerRepository.save(any(DeliveryManager.class))).thenReturn(deliveryManager);

            // when
            Long deliveryManagerId = deliveryService.createDeliveryManager(request);

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
                    DeliveryManagerType.fromValue(request.getDeliveryManagerType()).get(),
                    request.getHubId(), 1L, request.getName());
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
            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(deliveryManagerId, DeliveryManagerType.HUB_DELIVERY_MANAGER, 1L, 1L, "사용자1");
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
            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.HUB_DELIVERY_MANAGER, 1L, 1L, "사용자1");
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
                    .userId(1L)
                    .name("익명")
                    .limit(10)
                    .build();

            DeliveryManager deliveryManager1 = DeliveryManager.createDeliveryManager(2L, DeliveryManagerType.fromValue("허브 배송 담당자").get(), 1L, 2L, "사원1");
            DeliveryManager deliveryManager2 = DeliveryManager.createDeliveryManager(3L, DeliveryManagerType.fromValue("허브 배송 담당자").get(), 1L, 3L, "사원2");

            ReflectionTestUtils.setField(deliveryManager1, "id", 2L);
            ReflectionTestUtils.setField(deliveryManager2, "id", 3L);


            List<GetDeliveryManagerResponseDto> responseDtos = List.of(GetDeliveryManagerResponseDto.from(deliveryManager1), GetDeliveryManagerResponseDto.from(deliveryManager2));
            long cursorId = responseDtos.stream().mapToLong(GetDeliveryManagerResponseDto::getDeliveryManagerId)
                    .max().getAsLong();

            CursorPage<GetDeliveryManagerResponseDto> cursorPage = new CursorPage<>(responseDtos,
                    new CursorInfo(cursorId),
                    true);

            // when
            when(customDeliveryMangerRepository
                    .getDeliveryManagers(request.getDeliveryManagerId(), request.getSortBy(),
                            DeliveryManagerType.fromValue(request.getDeliveryManagerType()).get(), request.getUserId(), request.getName(), request.getLimit()))
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

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정전 이름");

            DeliveryManager updatedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(updatedDeliveryManager, "id", 1L);

            // when
            when(deliveryManagerRepository.findById(1L)).thenReturn(Optional.of(deliveryManager));
            when(deliveryManagerRepository.save(deliveryManager)).thenReturn(updatedDeliveryManager);

            // then
            UpdateDeliveryManagerResponseDto updateDeliveryManagerResponseDto = deliveryService.updateDeliveryManager(toBeUpdatedDeliveryManagerId, request);
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

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정전 이름");

            DeliveryManager updatedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(updatedDeliveryManager, "id", 1L);

            // when
            when(deliveryManagerRepository.findById(1L)).thenReturn(Optional.of(deliveryManager));
            when(deliveryManagerRepository.save(deliveryManager)).thenReturn(updatedDeliveryManager);

            // then
            UpdateDeliveryManagerResponseDto updateDeliveryManagerResponseDto = deliveryService.updateDeliveryManager(toBeUpdatedDeliveryManagerId, request);
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

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정전 이름");

            DeliveryManager updatedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(updatedDeliveryManager, "id", 1L);

            // when
            when(deliveryManagerRepository.findById(1L)).thenReturn(Optional.of(deliveryManager));

            // then
            Assertions.assertThatThrownBy(() -> deliveryService.updateDeliveryManager(toBeUpdatedDeliveryManagerId, request))
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

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "이름");

            DeliveryManager deletedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deletedDeliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deliveryManager, "deletedBy", UserContext.getUserContext().getUserId());

            // when
            when(deliveryManagerRepository.findById(toBeDeletedDeliveryManagerId)).thenReturn(Optional.of(deliveryManager));
            when(deliveryManagerRepository.save(deliveryManager)).thenReturn(deletedDeliveryManager);

            // then
            DeleteDeliveryManagerResponseDto response = deliveryService.deleteDeliveryManager(toBeDeletedDeliveryManagerId);
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

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "이름");

            DeliveryManager deletedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deletedDeliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deliveryManager, "deletedBy", UserContext.getUserContext().getUserId());

            // when
            when(deliveryManagerRepository.findById(toBeDeletedDeliveryManagerId)).thenReturn(Optional.of(deliveryManager));
            when(deliveryManagerRepository.save(deliveryManager)).thenReturn(deletedDeliveryManager);

            // then
            DeleteDeliveryManagerResponseDto response = deliveryService.deleteDeliveryManager(toBeDeletedDeliveryManagerId);
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

            DeliveryManager deliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "이름");

            DeliveryManager deletedDeliveryManager = DeliveryManager.createDeliveryManager(1L, DeliveryManagerType.STORE_DELIVERY_MANAGER, 1L,
                    1L, "수정한 사람");

            ReflectionTestUtils.setField(deliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deletedDeliveryManager, "id", 1L);
            ReflectionTestUtils.setField(deliveryManager, "deletedBy", UserContext.getUserContext().getUserId());

            // when
            when(deliveryManagerRepository.findById(toBeDeletedDeliveryManagerId)).thenReturn(Optional.of(deliveryManager));

            // then
            Assertions.assertThatThrownBy(() -> deliveryService.deleteDeliveryManager(toBeDeletedDeliveryManagerId))
                    .isInstanceOf(DeliveryManagerException.UnauthorizedDeliveryManagerDeleteException.class);

        }
    }
}