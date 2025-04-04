package com.example.consumer.service;

import com.example.consumer.model.BikeTrip;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;

    @Value("${kafka.topic}")
    private String topic;

    @KafkaListener(topics = "${kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBikeTrip(String bikeTripJson) {
        try {
            BikeTrip bikeTrip = objectMapper.readValue(bikeTripJson, BikeTrip.class);
            log.info("Consumer received bike trip from topic {}: Trip ID: {}, From: {}, To: {}, Duration: {} seconds",
                    topic,
                    bikeTrip.getTripId(),
                    bikeTrip.getFromStationName(),
                    bikeTrip.getToStationName(),
                    bikeTrip.getTripDuration());
        } catch (JsonProcessingException e) {
            log.error("Error deserializing bike trip from topic {}: {}", topic, e.getMessage(), e);
        }
    }
}