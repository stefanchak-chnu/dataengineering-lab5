package com.example.consumer.service;

import com.example.consumer.model.BikeTrip;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper;
    private final String minioBucket;
    private final CsvFileManagementService csvFileService;
    private final MinioFileUploadService minioStorageService;

    @Value("${kafka.topic}")
    private String topic;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get("csv_files"));
        } catch (IOException e) {
            log.error("Error creating directory for CSV files: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBikeTrip(String bikeTripJson) {
        try {
            BikeTrip bikeTrip = deserializeBikeTrip(bikeTripJson);
            logReceivedTrip(bikeTrip);
            processBikeTrip(bikeTrip);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing bike trip from topic {}: {}", topic, e.getMessage(), e);
        }
    }

    private BikeTrip deserializeBikeTrip(String bikeTripJson) throws JsonProcessingException {
        return objectMapper.readValue(bikeTripJson, BikeTrip.class);
    }

    private void logReceivedTrip(BikeTrip bikeTrip) {
        log.info("Consumer received bike trip from topic {}: Trip ID: {}, From: {}, To: {}, Duration: {} seconds",
                topic,
                bikeTrip.getTripId(),
                bikeTrip.getFromStationName(),
                bikeTrip.getToStationName(),
                bikeTrip.getTripDuration());
    }

    private void processBikeTrip(BikeTrip bikeTrip) {
        try {
            LocalDateTime startTime = parseDateTime(bikeTrip.getStartTime());
            YearMonth yearMonth = YearMonth.from(startTime);

            if (csvFileService.shouldRotateFile(yearMonth)) {
                uploadPreviousMonthFile();
            }

            csvFileService.writeTrip(bikeTrip, yearMonth);
        } catch (IOException e) {
            log.error("Error processing bike trip: {}", e.getMessage(), e);
        }
    }

    public LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (DateTimeParseException e) {
            log.warn("Error parsing date: {}. Using epoch start instead.", dateTimeStr);
            return LocalDateTime.MIN;
        }
    }

    private void uploadPreviousMonthFile() {
        YearMonth previousMonth = csvFileService.getCurrentMonth();
        if (previousMonth == null) {
            return;
        }

        String fileName = csvFileService.getFileName(previousMonth);
        csvFileService.closeCurrentPrinter();

        try {
            minioStorageService.uploadFileToMinio(fileName, minioBucket);
        } catch (Exception e) {
            log.error("Error uploading file {} to MinIO: {}", fileName, e.getMessage(), e);
        }
    }
}