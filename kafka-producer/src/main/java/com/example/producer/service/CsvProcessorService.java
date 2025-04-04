package com.example.producer.service;

import com.example.producer.model.BikeTrip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CsvProcessorService {

    private final CsvReaderService csvReaderService;
    private final KafkaProducerService kafkaProducerService;
    
    @Value("${csv.file.path}")
    private String csvFilePath;
    
    @Scheduled(fixedDelay = 60000)
    public void processCsvAndSendBikeTrips() {
        log.info("Starting to process bike trips CSV file: {}", csvFilePath);
        
        List<BikeTrip> bikeTrips = csvReaderService.readBikeTripsFromCsv(csvFilePath);
        
        for (BikeTrip bikeTrip : bikeTrips) {
            kafkaProducerService.sendBikeTripToTopics(bikeTrip);
        }
        
        log.info("Finished processing {} bike trips from CSV file", bikeTrips.size());
    }
}