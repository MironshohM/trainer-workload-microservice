package com.epam.microservices.TrainingSummary.service;

import com.epam.microservices.TrainingSummary.dtos.TrainingSessionEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;



@Component
public class TrainingSessionEventListener {

    private final TrainingWorkloadService workloadService;
    private static final Logger logger = LoggerFactory.getLogger(TrainingSessionEventListener.class);

    public TrainingSessionEventListener(TrainingWorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @JmsListener(destination = "training-session-events")
    public void receiveTrainingSessionEvent(TrainingSessionEventDTO dto) {
        logger.info("Received training session event: {}", dto);

        try {
            workloadService.processTrainingSessionEvent(dto);
        } catch (Exception e) {
            logger.error("Failed to process training session event: {}", e.getMessage(), e);
        }
    }
}