package com.example.producer.service;

import com.example.producer.model.BikeTrip;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.topic1}")
    private String topic1;

    @Value("${kafka.topic.topic2}")
    private String topic2;

    public void sendBikeTripToTopics(BikeTrip bikeTrip) {
        try {
            String tripJson = objectMapper.writeValueAsString(bikeTrip);

            kafkaTemplate.send(topic1, bikeTrip.getTripId(), tripJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Bike trip sent to topic {}: Trip ID {}", topic1, bikeTrip.getTripId());
                        } else {
                            log.error("Failed to send bike trip to topic {}: {}", topic1, ex.getMessage(), ex);
                        }
                    });

            kafkaTemplate.send(topic2, bikeTrip.getTripId(), tripJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Bike trip sent to topic {}: Trip ID {}", topic2, bikeTrip.getTripId());
                        } else {
                            log.error("Failed to send bike trip to topic {}: {}", topic2, ex.getMessage(), ex);
                        }
                    });

        } catch (JsonProcessingException e) {
            log.error("Error serializing bike trip: {}", e.getMessage(), e);
        }
    }
}