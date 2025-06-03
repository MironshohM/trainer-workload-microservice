package com.epam.microservices.TrainingSummary.service;

import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryDTO;
import com.epam.microservices.TrainingSummary.dtos.TrainingSessionEventDTO;

public interface TrainingWorkloadService {
    void processTrainingSessionEvent(TrainingSessionEventDTO dto);
    MonthlySummaryDTO getMonthlySummary(String username, int year, int month);
}
