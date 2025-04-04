package com.example.producer.service;

import com.example.producer.model.BikeTrip;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvReaderService {

    public List<BikeTrip> readBikeTripsFromCsv(String filePath) {
        List<BikeTrip> bikeTrips = new ArrayList<>();

        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase(true)
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                BikeTrip bikeTrip = new BikeTrip();
                bikeTrip.setTripId(record.get("trip_id"));
                bikeTrip.setStartTime(record.get("start_time"));
                bikeTrip.setEndTime(record.get("end_time"));
                bikeTrip.setBikeId(record.get("bikeid"));

                try {
                    bikeTrip.setTripDuration(Double.parseDouble(record.get("tripduration")));
                } catch (NumberFormatException e) {
                    bikeTrip.setTripDuration(0.0);
                    log.warn("Invalid trip duration for trip {}: {}", bikeTrip.getTripId(), e.getMessage());
                }

                bikeTrip.setFromStationId(record.get("from_station_id"));
                bikeTrip.setFromStationName(record.get("from_station_name"));
                bikeTrip.setToStationId(record.get("to_station_id"));
                bikeTrip.setToStationName(record.get("to_station_name"));
                bikeTrip.setUserType(record.get("usertype"));
                bikeTrip.setGender(record.get("gender"));
                bikeTrip.setBirthYear(record.get("birthyear"));

                bikeTrips.add(bikeTrip);
            }

            log.info("Read {} bike trips from CSV file", bikeTrips.size());
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage(), e);
        }

        return bikeTrips;
    }
}