package com.epam.microservices.TrainingSummary.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyWorkload {

    @Id
    private String id; // MongoDB uses String/ObjectId

    private int month; // 1 = January, 12 = December

    private int totalTrainingDuration; // in minutes or hours
}
