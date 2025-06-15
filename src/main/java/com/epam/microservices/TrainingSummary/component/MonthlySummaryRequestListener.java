package com.epam.microservices.TrainingSummary.component;

import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryDTO;
import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryRequest;
import com.epam.microservices.TrainingSummary.service.TrainingWorkloadService;
import jakarta.jms.Destination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class MonthlySummaryRequestListener {

    private static final Logger logger = LoggerFactory.getLogger(MonthlySummaryRequestListener.class);

    private final TrainingWorkloadService workloadService;
    private final JmsTemplate jmsTemplate;

    public MonthlySummaryRequestListener(TrainingWorkloadService workloadService, JmsTemplate jmsTemplate) {
        this.workloadService = workloadService;
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "monthly.summary.request.queue")
    public void handleSummaryRequest(MonthlySummaryRequest request, @Header("JMSReplyTo") Destination replyTo) {
        try {
            MonthlySummaryDTO summary = workloadService.getMonthlySummary(request.getUsername(), request.getYear(), request.getMonth());
            jmsTemplate.convertAndSend(replyTo, summary);
        } catch (Exception e) {
            logger.error("Error processing monthly summary request", e);
        }
    }
}
