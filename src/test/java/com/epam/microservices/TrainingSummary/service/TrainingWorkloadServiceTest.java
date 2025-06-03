package com.epam.microservices.TrainingSummary.service;


import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryDTO;
import com.epam.microservices.TrainingSummary.dtos.TrainingSessionEventDTO;
import com.epam.microservices.TrainingSummary.model.MonthlyWorkload;
import com.epam.microservices.TrainingSummary.model.TrainerWorkload;
import com.epam.microservices.TrainingSummary.model.YearlyWorkload;
import com.epam.microservices.TrainingSummary.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainingWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository trainerRepo;

    @InjectMocks
    private TrainingWorkloadServiceImpl workloadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        MDC.put("transactionId", "test-tx-id");
    }

    @Test
    void testProcessTrainingSessionEvent_AddToExistingTrainer() {
        TrainingSessionEventDTO dto = new TrainingSessionEventDTO("trainer1", "John", "Doe", true,
                LocalDate.of(2024, 5, 1), 60, "ADD");

        TrainerWorkload trainer = new TrainerWorkload("trainer1", "John", "Doe", true);
        YearlyWorkload yearly = new YearlyWorkload();
        yearly.setYear(2024);
        MonthlyWorkload monthly = new MonthlyWorkload();
        monthly.setMonth(5);
        yearly.getMonthlyWorkloads().add(monthly);
        trainer.getYearlyWorkloads().add(yearly);

        when(trainerRepo.findById("trainer1")).thenReturn(Optional.of(trainer));

        workloadService.processTrainingSessionEvent(dto);

        assertEquals(60, monthly.getTotalTrainingDuration());
        verify(trainerRepo).save(trainer);
    }

    @Test
    void testProcessTrainingSessionEvent_NewTrainer() {
        TrainingSessionEventDTO dto = new TrainingSessionEventDTO("trainer2", "Jane", "Smith", true,
                LocalDate.of(2024, 6, 15), 45, "ADD");

        when(trainerRepo.findById("trainer2")).thenReturn(Optional.empty());

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);

        workloadService.processTrainingSessionEvent(dto);

        verify(trainerRepo).save(captor.capture());
        TrainerWorkload saved = captor.getValue();

        assertEquals("Jane", saved.getFirstName());
        assertEquals(1, saved.getYearlyWorkloads().size());
        assertEquals(1, saved.getYearlyWorkloads().get(0).getMonthlyWorkloads().size());
        assertEquals(45, saved.getYearlyWorkloads().get(0).getMonthlyWorkloads().get(0).getTotalTrainingDuration());
    }

    @Test
    void testGetMonthlySummary_WithData() {
        TrainerWorkload trainer = new TrainerWorkload("trainer3", "Jim", "Beam", true);
        YearlyWorkload yearly = new YearlyWorkload();
        yearly.setYear(2023);
        MonthlyWorkload monthly = new MonthlyWorkload();
        monthly.setMonth(12);
        monthly.setTotalTrainingDuration(90);
        yearly.getMonthlyWorkloads().add(monthly);
        trainer.getYearlyWorkloads().add(yearly);

        when(trainerRepo.findById("trainer3")).thenReturn(Optional.of(trainer));

        MonthlySummaryDTO summary = workloadService.getMonthlySummary("trainer3", 2023, 12);

        assertEquals(90, summary.getTotalTrainingDuration());
        assertEquals("trainer3", summary.getUsername());
    }

    @Test
    void testGetMonthlySummary_NoData_ReturnsZero() {
        TrainerWorkload trainer = new TrainerWorkload("trainer4", "Amy", "Lee", true);
        when(trainerRepo.findById("trainer4")).thenReturn(Optional.of(trainer));

        MonthlySummaryDTO summary = workloadService.getMonthlySummary("trainer4", 2023, 11);

        assertEquals(0, summary.getTotalTrainingDuration());
        assertEquals("trainer4", summary.getUsername());
    }

    @Test
    void testGetMonthlySummary_TrainerNotFound() {
        when(trainerRepo.findById("unknown"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> workloadService.getMonthlySummary("unknown", 2024, 5));

        assertEquals("Trainer not found", exception.getMessage());
    }
}
