package com.example.producer.service;

import com.example.producer.model.BikeTrip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
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

        bikeTrips.sort(Comparator.comparing(trip -> parseDateTime(trip.getStartTime())));

        for (BikeTrip bikeTrip : bikeTrips) {
            kafkaProducerService.sendBikeTripToTopics(bikeTrip);
        }

        log.info("Finished processing {} bike trips from CSV file", bikeTrips.size());
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            log.warn("Error parsing date: {}. Using epoch start instead.", dateTimeStr);
            return LocalDateTime.MIN; // Use minimum date as fallback
        }
    }
}
