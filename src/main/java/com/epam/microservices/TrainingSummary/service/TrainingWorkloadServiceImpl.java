package com.epam.microservices.TrainingSummary.service;

import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryDTO;
import com.epam.microservices.TrainingSummary.dtos.TrainingSessionEventDTO;
import com.epam.microservices.TrainingSummary.model.MonthlyWorkload;
import com.epam.microservices.TrainingSummary.model.TrainerWorkload;
import com.epam.microservices.TrainingSummary.model.YearlyWorkload;
import com.epam.microservices.TrainingSummary.repository.TrainerWorkloadRepository;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Optional;

@Service
@Slf4j
public class TrainingWorkloadServiceImpl implements TrainingWorkloadService {

    private final TrainerWorkloadRepository trainerRepo;

    public TrainingWorkloadServiceImpl(TrainerWorkloadRepository trainerRepo) {
        this.trainerRepo = trainerRepo;
    }

    @Override
    public void processTrainingSessionEvent(TrainingSessionEventDTO dto) {
        String transactionId = MDC.get("transactionId");
        log.info("TransactionID: {} - Processing training session event: {}", transactionId, dto);

        String username = dto.getUsername();
        int year = dto.getTrainingDate().getYear();
        int month = dto.getTrainingDate().getMonthValue();
        int duration = dto.getTrainingDuration();
        String action = dto.getActionType();

        // Check if trainer exists
        Optional<TrainerWorkload> optionalTrainer = trainerRepo.findById(username);
        TrainerWorkload trainer;

        if (optionalTrainer.isEmpty()) {
            // New trainer case
            log.info("TransactionID: {} - Creating new TrainerWorkload for {}", transactionId, username);

            MonthlyWorkload monthly = new MonthlyWorkload();
            monthly.setMonth(month);
            monthly.setTotalTrainingDuration(duration); // set initial value

            YearlyWorkload yearly = new YearlyWorkload();
            yearly.setYear(year);
            yearly.getMonthlyWorkloads().add(monthly);

            trainer = new TrainerWorkload(
                    username,
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.isActive()
            );
            trainer.getYearlyWorkloads().add(yearly);

            log.info("TransactionID: {} - New trainer document initialized with year {} and month {}", transactionId, year, month);
        } else {
            // Existing trainer case
            trainer = optionalTrainer.get();
            trainer.setFirstName(dto.getFirstName());
            trainer.setLastName(dto.getLastName());
            trainer.setActive(dto.isActive());

            YearlyWorkload yearly = trainer.getYearlyWorkloads().stream()
                    .filter(y -> y.getYear() == year)
                    .findFirst()
                    .orElseGet(() -> {
                        YearlyWorkload y = new YearlyWorkload();
                        y.setYear(year);
                        trainer.getYearlyWorkloads().add(y);
                        return y;
                    });

            MonthlyWorkload monthly = yearly.getMonthlyWorkloads().stream()
                    .filter(m -> m.getMonth() == month)
                    .findFirst()
                    .orElseGet(() -> {
                        MonthlyWorkload m = new MonthlyWorkload();
                        m.setMonth(month);
                        yearly.getMonthlyWorkloads().add(m);
                        return m;
                    });

            if ("ADD".equalsIgnoreCase(action)) {
                log.info("TransactionID: {} - Adding {} minutes for {}/{}", transactionId, duration, year, month);
                int current = monthly.getTotalTrainingDuration();
                monthly.setTotalTrainingDuration(current + duration); // d) + e)
            } else if ("DELETE".equalsIgnoreCase(action)) {
                log.info("TransactionID: {} - Removing {} minutes for {}/{}", transactionId, duration, year, month);
                int current = monthly.getTotalTrainingDuration();
                monthly.setTotalTrainingDuration(Math.max(0, current - duration)); // safe floor at 0
            }


            trainerRepo.save(trainer);
            log.info("TransactionID: {} - Updated trainer workload saved for {}", transactionId, username);
        }
    }


    @Override
    public MonthlySummaryDTO getMonthlySummary(String username, int year, int month) {
        String transactionId = MDC.get("transactionId");
        log.info("TransactionID: {} - Fetching monthly summary for {}, Year: {}, Month: {}", transactionId, username, year, month);

        TrainerWorkload trainer = trainerRepo.findById(username)
                .orElseThrow(() -> {
                    log.error("TransactionID: {} - Trainer {} not found", transactionId, username);
                    return new RuntimeException("Trainer not found");
                });

        YearlyWorkload yearly = trainer.getYearlyWorkloads().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElse(null);

        if (yearly == null) {
            log.info("TransactionID: {} - No yearly data found for {} in {}", transactionId, username, year);
            return new MonthlySummaryDTO(username, trainer.getFirstName(), trainer.getLastName(),
                    trainer.isActive(), year, month, 0);
        }

        MonthlyWorkload monthly = yearly.getMonthlyWorkloads().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElse(null);

        int total = (monthly != null) ? monthly.getTotalTrainingDuration() : 0;
        log.info("TransactionID: {} - Summary for {}: {} minutes in {}/{}", transactionId, username, total, year, month);

        return new MonthlySummaryDTO(username, trainer.getFirstName(), trainer.getLastName(),
                trainer.isActive(), year, month, total);
    }
}
