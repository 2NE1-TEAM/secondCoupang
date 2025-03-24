package com.toanyone.hub.common.config.kafkaConfig;

import com.toanyone.hub.domain.model.Hub;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, Hub> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
        // ValueDeserializer로 Hub 객체를 처리할 JsonDeserializer 사용
        JsonDeserializer<Hub> hubDeserializer = new JsonDeserializer<>(Hub.class);
        hubDeserializer.setRemoveTypeHeaders(false);
        hubDeserializer.addTrustedPackages("com.toanyone.hub.infrastructure.messaging.dto");  // Trust Hub class for deserialization

        // ErrorHandlingDeserializer를 사용하여 예외를 처리
        ErrorHandlingDeserializer<Hub> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(hubDeserializer);

        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), errorHandlingDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Hub> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Hub> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
//        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
//        factory.getContainerProperties().setIdleBetweenPolls(20000L);
        return factory;
    }
}