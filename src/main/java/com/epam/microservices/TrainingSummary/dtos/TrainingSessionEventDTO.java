package com.epam.microservices.TrainingSummary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TrainingSessionEventDTO {
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private LocalDate trainingDate;
    private int trainingDuration; // in minutes or hours
    private String actionType; // "ADD" or "DELETE"

    public TrainingSessionEventDTO() {

    }
}
