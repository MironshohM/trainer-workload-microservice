package com.epam.microservices.TrainingSummary.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummaryRequest implements Serializable {
    private String username;
    private int year;
    private int month;
}