package com.epam.microservices.TrainingSummary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MonthlySummaryDTO {
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private int year;
    private int month;
    private int totalTrainingDuration;

    public MonthlySummaryDTO(String username, String firstName, String lastName, boolean active, int year, int month, int i) {

        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = active;
        this.year = year;
        this.month = month;
        this.totalTrainingDuration = i;

    }
}
