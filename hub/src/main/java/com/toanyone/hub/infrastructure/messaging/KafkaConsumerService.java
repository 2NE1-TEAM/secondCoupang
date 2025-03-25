package com.toanyone.hub.infrastructure.messaging;

import com.toanyone.hub.domain.exception.HubException;
import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.repository.HubRepository;
import com.toanyone.hub.domain.service.HubService;
import com.toanyone.hub.domain.service.RouteService;
import com.toanyone.hub.infrastructure.client.SlackClient;
import com.toanyone.hub.infrastructure.client.dto.RequestCreateMessageDto;
import com.toanyone.hub.infrastructure.messaging.dto.HubCreateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@EnableKafka
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final HubService hubService;
    private final KafkaProducerService kafkaProducerService;
    private final RouteService routeService;
    private final HubRepository hubRepository;
    private final SlackClient slackClient;

    @KafkaListener(topics = "hub-create", groupId = "my-group1")
    public void consumeHubCreateMessage(HubCreateMessage hubCreateMessage) {
        try {
            log.info("허브간 거리 데이터 생성하는 리스너 :: KafkaConsumerService :: consumeHubCreateMessage :: {}", hubCreateMessage);
            routeService.addHubDistances(hubCreateMessage);
            kafkaProducerService.sendSuccessMessage(hubCreateMessage);
        } catch (Exception e) {
            log.info("허브간 거리 데이터 생성하는 리스너 에외 발생 :: KafkaConsumerService :: consumeHubCreateMessage", e);
            kafkaProducerService.sendFailureMessage(hubCreateMessage);
        }
    }

    @KafkaListener(topics = "hub-create-success", groupId = "my-group2")
    public void consumeHubCreateSuccessMessage(HubCreateMessage hubCreateMessage) {
        log.info("허브간 거리 데이터 생성 성공 리스너 :: KafkaConsumerService :: consumeHubCreateSuccessMessage :: {}", hubCreateMessage);
        // 사용자에게 성공했다고 슬랙 보내기 - to do
        slackClient.sendSlackMessage(hubCreateMessage.getRole(), hubCreateMessage.getSlackId(), hubCreateMessage.getUserId(), new RequestCreateMessageDto(hubCreateMessage.getSlackId(), "허브 생성에 성공했습니다."));
    }

    @KafkaListener(topics = "hub-create-failure", groupId = "my-group3")
    public void consumeHubCreateFailureMessage(HubCreateMessage hubCreateMessage) {
        log.info("허브간 거리 데이터 생성 실패 리스너 :: KafkaConsumerService :: consumeHubCreateFailureMessage :: {}" , hubCreateMessage);
        Hub findHub = hubRepository.findById(hubCreateMessage.getHubId()).orElseThrow(()->
                new HubException.HubNotFoundException("허브가 존재하지 않습니다."));
        hubService.deleteHub(findHub.getId(), hubCreateMessage.getUserId()); // 비동기라서 이미 생성된 허브 삭제
        // 사용자에게 허브 생성 실패했다고 슬랙 보내기 - to do
        slackClient.sendSlackMessage(hubCreateMessage.getRole(), hubCreateMessage.getSlackId(), hubCreateMessage.getUserId(),  new RequestCreateMessageDto(hubCreateMessage.getSlackId(), "허브 생성에 실패했습니다. 다시 시도해 주세요."));

    }
}
