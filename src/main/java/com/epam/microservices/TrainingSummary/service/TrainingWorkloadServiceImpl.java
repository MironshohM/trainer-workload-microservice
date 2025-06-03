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

@Service
@Slf4j
public class TrainingWorkloadServiceImpl implements TrainingWorkloadService {

    private final TrainerWorkloadRepository trainerRepo;

    public TrainingWorkloadServiceImpl(TrainerWorkloadRepository trainerRepo) {
        this.trainerRepo = trainerRepo;
    }

    @Override
    public void processTrainingSessionEvent(TrainingSessionEventDTO dto) {
        String transactionId = MDC.get("transactionId"); // get from context if available
        log.info("TransactionID: {} - Processing training session event: {}", transactionId, dto);

        TrainerWorkload trainer = trainerRepo.findById(dto.getUsername())
                .orElseGet(() -> {
                    log.info("TransactionID: {} - Creating new TrainerWorkload for {}", transactionId, dto.getUsername());
                    return new TrainerWorkload(
                            dto.getUsername(),
                            dto.getFirstName(),
                            dto.getLastName(),
                            dto.isActive()
                    );
                });

        trainer.setFirstName(dto.getFirstName());
        trainer.setLastName(dto.getLastName());
        trainer.setActive(dto.isActive());

        int year = dto.getTrainingDate().getYear();
        int month = dto.getTrainingDate().getMonthValue();

        YearlyWorkload yearly = trainer.getYearlyWorkloads().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    log.info("TransactionID: {} - Creating new YearlyWorkload for year {}", transactionId, year);
                    YearlyWorkload y = new YearlyWorkload();
                    y.setYear(year);
                    trainer.getYearlyWorkloads().add(y);
                    return y;
                });

        MonthlyWorkload monthly = yearly.getMonthlyWorkloads().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    log.info("TransactionID: {} - Creating new MonthlyWorkload for month {}", transactionId, month);
                    MonthlyWorkload m = new MonthlyWorkload();
                    m.setMonth(month);
                    yearly.getMonthlyWorkloads().add(m);
                    return m;
                });

        if ("ADD".equalsIgnoreCase(dto.getActionType())) {
            log.info("TransactionID: {} - Adding {} minutes to {} for {}/{}", transactionId,
                    dto.getTrainingDuration(), dto.getUsername(), year, month);
            monthly.setTotalTrainingDuration(monthly.getTotalTrainingDuration() + dto.getTrainingDuration());
        } else if ("DELETE".equalsIgnoreCase(dto.getActionType())) {
            log.info("TransactionID: {} - Removing {} minutes from {} for {}/{}", transactionId,
                    dto.getTrainingDuration(), dto.getUsername(), year, month);
            monthly.setTotalTrainingDuration(
                    Math.max(0, monthly.getTotalTrainingDuration() - dto.getTrainingDuration())
            );
        }

        trainerRepo.save(trainer);
        log.info("TransactionID: {} - Trainer workload saved for {}", transactionId, dto.getUsername());
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
