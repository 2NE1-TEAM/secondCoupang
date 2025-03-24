package com.toanyone.hub.infrastructure.messaging;

import com.toanyone.hub.infrastructure.messaging.dto.HubCreateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // 허브간 거리 생성을 하라는 메시지 전송
    public void createHub(HubCreateMessage hubCreateMessage) {
        log.info("hub-create 메시지 생성 프로듀서 :: {}", hubCreateMessage);
        kafkaTemplate.send("hub-create", hubCreateMessage);
    }

    // 허브간 거리 생성 완료 메시지 전송
    public void sendSuccessMessage(HubCreateMessage hubCreateMessage) {
        log.info("hub-create-success 메시지 생성 프로듀서 :: {}", hubCreateMessage);
        kafkaTemplate.send("hub-create-success", hubCreateMessage);
    }

    // 허브 생성 실패 메시지 전송
    public void sendFailureMessage(HubCreateMessage hubCreateMessage) {
        log.info("hub-create-failure 메시지 생성 프로듀서 :: {} ", hubCreateMessage);
        kafkaTemplate.send("hub-create-failure", hubCreateMessage);
    }
}
