package com.toanyone.hub.infrastructure.messaging;

import com.toanyone.hub.infrastructure.messaging.dto.HubCreateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class HubCreateEventHandler {

    private final KafkaProducerService kafkaProducerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHubCreatedEvent(HubCreateMessage hubCreateMessage) {
        log.info("트랜잭션 커밋 후 Kafka 메시지 전송 :: hubId={}, slackId={}", hubCreateMessage.getHubId(), hubCreateMessage.getSlackId());
        kafkaProducerService.createHub(hubCreateMessage);
    }
}