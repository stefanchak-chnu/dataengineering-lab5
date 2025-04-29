package com.example.consumer.service;

import com.example.consumer.model.BikeTrip;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CsvFileManagementService {

    private final Map<YearMonth, CSVPrinter> monthlyPrinters = new ConcurrentHashMap<>();
    
    @Getter
    private YearMonth currentMonth = null;
    
    public boolean shouldRotateFile(YearMonth newMonth) {
        return currentMonth != null && !newMonth.equals(currentMonth);
    }
    
    public void writeTrip(BikeTrip bikeTrip, YearMonth yearMonth) throws IOException {
        currentMonth = yearMonth;
        CSVPrinter csvPrinter = getOrCreateCsvPrinter(yearMonth);
        
        csvPrinter.printRecord(
                bikeTrip.getTripId(),
                bikeTrip.getStartTime(),
                bikeTrip.getEndTime(),
                bikeTrip.getBikeId(),
                bikeTrip.getTripDuration(),
                bikeTrip.getFromStationId(),
                bikeTrip.getFromStationName(),
                bikeTrip.getToStationId(),
                bikeTrip.getToStationName(),
                bikeTrip.getUserType(),
                bikeTrip.getGender(),
                bikeTrip.getBirthYear()
        );
        csvPrinter.flush();
    }
    
    public void closeCurrentPrinter() {
        if (currentMonth == null) {
            return;
        }
        
        try {
            CSVPrinter csvPrinter = monthlyPrinters.remove(currentMonth);
            if (csvPrinter != null) {
                csvPrinter.close();
            }
        } catch (IOException e) {
            log.error("Error closing CSV printer: {}", e.getMessage(), e);
        }
    }
    
    private CSVPrinter getOrCreateCsvPrinter(YearMonth yearMonth) throws IOException {
        return monthlyPrinters.computeIfAbsent(yearMonth, ym -> {
            try {
                String fileName = getFileName(ym);
                FileWriter fileWriter = new FileWriter(fileName, true);
                Path path = Paths.get(fileName);

                CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                        .setHeader("trip_id", "start_time", "end_time", "bike_id", "trip_duration",
                                "from_station_id", "from_station_name", "to_station_id", "to_station_name",
                                "user_type", "gender", "birth_year")
                        .setSkipHeaderRecord(Files.exists(path) && Files.size(path) > 0)
                        .build();

                return new CSVPrinter(fileWriter, csvFormat);
            } catch (IOException e) {
                log.error("Error creating CSV printer for {}: {}", ym, e.getMessage(), e);
                throw new RuntimeException("Error creating CSV printer", e);
            }
        });
    }
    
    public String getFileName(YearMonth yearMonth) {
        String month = yearMonth.getMonth().toString().toLowerCase();
        int year = yearMonth.getYear();
        return String.format("csv_files/%s_%d.csv", month, year);
    }
}