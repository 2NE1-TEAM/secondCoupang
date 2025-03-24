package com.toanyone.hub.infrastructure.messaging;

import com.toanyone.hub.domain.exception.HubException;
import com.toanyone.hub.domain.model.Hub;
import com.toanyone.hub.domain.repository.HubRepository;
import com.toanyone.hub.domain.service.HubService;
import com.toanyone.hub.domain.service.RouteService;
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

//    @Transactional
    @KafkaListener(topics = "hub-create", groupId = "my-group1")
    public void consumeHubCreateMessage(Hub hub) {
        try {
            log.info("허브간 거리 데이터 생성하는 리스너 :: KafkaConsumerService :: consumeHubCreateMessage :: {}", hub);
            routeService.addHubDistances(hub);
            kafkaProducerService.sendSuccessMessage(hub);
//            System.out.println("리스너 동작");
        } catch (Exception e) {
            log.info("허브간 거리 데이터 생성하는 리스너 에외 발생 :: KafkaConsumerService :: consumeHubCreateMessage", e);
            kafkaProducerService.sendFailureMessage(hub);
//            throw new RuntimeException("리스너 실패");
//            ack.acknowledge();
        }
    }

    @KafkaListener(topics = "hub-create-success", groupId = "my-group2")
    public void consumeHubCreateSuccessMessage(Hub hub) {
        log.info("허브간 거리 데이터 생성 성공 리스너 :: KafkaConsumerService :: consumeHubCreateSuccessMessage :: {}", hub);
        // 사용자에게 성공했다고 슬랙 보내기 - to do
    }

    @KafkaListener(topics = "hub-create-failure", groupId = "my-group3")
    public void consumeHubCreateFailureMessage(Hub hub) {
        log.info("허브간 거리 데이터 생성 실패 리스너 :: KafkaConsumerService :: consumeHubCreateFailureMessage :: {}" , hub);
        Hub findHub = hubRepository.findById(hub.getId()).orElseThrow(()->
                new HubException.HubNotFoundException("허브가 존재하지 않습니다."));
        hubService.deleteHub(findHub.getId()); // 비동기라서 이미 생성된 허브 삭제
        // 사용자에게 허브 생성 실패했다고 슬랙 보내기 - to do
    }
}
