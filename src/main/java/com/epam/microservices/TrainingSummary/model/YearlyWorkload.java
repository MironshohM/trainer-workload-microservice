package com.epam.microservices.TrainingSummary.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YearlyWorkload {

    @Id
    private String id;

    private int year;

    private List<MonthlyWorkload> monthlyWorkloads = new ArrayList<>();
}
