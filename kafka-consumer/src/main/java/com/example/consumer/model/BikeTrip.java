package com.example.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BikeTrip {
    private String tripId;
    private String startTime;
    private String endTime;
    private String bikeId;
    private Double tripDuration;
    private String fromStationId;
    private String fromStationName;
    private String toStationId;
    private String toStationName;
    private String userType;
    private String gender;
    private String birthYear;
}