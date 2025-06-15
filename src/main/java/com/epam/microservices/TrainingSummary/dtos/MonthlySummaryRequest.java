package com.epam.microservices.TrainingSummary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryRequest {
    private String username;
    private int year;
    private int month;
}