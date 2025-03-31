package com.toanyone.delivery.application;

import com.toanyone.delivery.application.dtos.request.GetDeliverySearchConditionRequestDto;
import com.toanyone.delivery.application.dtos.request.UpdateDeliveryRequestDto;
import com.toanyone.delivery.application.dtos.response.DeleteDeliveryResponseDto;
import com.toanyone.delivery.application.dtos.response.GetDeliveryManagerResponseDto;
import com.toanyone.delivery.application.dtos.response.GetDeliveryResponseDto;
import com.toanyone.delivery.application.dtos.response.UpdateDeliveryResponseDto;
import com.toanyone.delivery.application.exception.DeliveryException;
import com.toanyone.delivery.common.utils.MultiResponse.CursorInfo;
import com.toanyone.delivery.common.utils.MultiResponse.CursorPage;
import com.toanyone.delivery.common.utils.UserContext;
import com.toanyone.delivery.domain.Delivery;
import com.toanyone.delivery.domain.DeliveryManager;
import com.toanyone.delivery.domain.DeliveryManager.DeliveryManagerType;
import com.toanyone.delivery.domain.DeliveryRoad;
import com.toanyone.delivery.domain.repository.CustomDeliveryRepository;
import com.toanyone.delivery.domain.repository.DeliveryRepository;
import com.toanyone.delivery.infrastructure.client.AiClient;
import com.toanyone.delivery.infrastructure.client.HubClient;
import com.toanyone.delivery.message.DeliveryCompletedMessage;
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
    private DeliveryManagerService deliveryManagerService;
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
                    .orderId(updatedDelivery.getOrderId())
                    .deliveryStatus(Delivery.DeliveryStatus.DELIVERY_COMPLETED.toString())
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
                    .orderId(updatedDelivery.getOrderId())
                    .deliveryStatus(Delivery.DeliveryStatus.DELIVERY_COMPLETED.toString())
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
                    .orderId(updatedDelivery.getOrderId())
                    .deliveryStatus(Delivery.DeliveryStatus.DELIVERY_COMPLETED.toString())
                    .build();

            Message<DeliveryCompletedMessage> kafkaMesage = MessageBuilder.withPayload(message)
                    .setHeader(KafkaHeaders.TOPIC, "delivery.completed")
                    .build();

            GetDeliveryManagerResponseDto getDeliveryManagerResponseDto = GetDeliveryManagerResponseDto.from(storeDeliveryManager);

            //when
            when(deliveryRepository.findById(toBeUpdatedDeliveryId)).thenReturn(Optional.of(delivery));
            when(deliveryRepository.save(delivery)).thenReturn(updatedDelivery);
            when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));
            when(deliveryManagerService.getDeliveryManager(storeDeliveryManagerId)).thenReturn(getDeliveryManagerResponseDto);

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
                    .orderId(updatedDelivery.getOrderId())
                    .deliveryStatus(Delivery.DeliveryStatus.DELIVERY_COMPLETED.toString())
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


}