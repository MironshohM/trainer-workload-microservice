package com.epam.microservices.TrainingSummary.cucumber;


import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryDTO;
import com.epam.microservices.TrainingSummary.dtos.TrainingSessionEventDTO;
import com.epam.microservices.TrainingSummary.model.MonthlyWorkload;
import com.epam.microservices.TrainingSummary.model.TrainerWorkload;
import com.epam.microservices.TrainingSummary.model.YearlyWorkload;
import com.epam.microservices.TrainingSummary.repository.TrainerWorkloadRepository;
import com.epam.microservices.TrainingSummary.service.TrainingWorkloadService;
import com.epam.microservices.TrainingSummary.service.TrainingWorkloadServiceImpl;
import io.cucumber.java.en.*;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainingWorkloadStepDefinitions {

    private final TrainerWorkloadRepository trainerRepo = Mockito.mock(TrainerWorkloadRepository.class);
    private final TrainingWorkloadService workloadService = new TrainingWorkloadServiceImpl(trainerRepo);

    private MonthlySummaryDTO summaryDTO;
    private final Map<String, TrainerWorkload> db = new HashMap<>();

    @Given("the trainer {string} does not exist in the database")
    public void trainer_does_not_exist(String username) {
        when(trainerRepo.findById(username)).thenReturn(Optional.empty());
    }

    @Given("the trainer {string} exists with {int} minutes in June 2025")
    public void trainer_exists_with_minutes(String username, int duration) {
        MonthlyWorkload monthly = new MonthlyWorkload(null, 6, duration);
        YearlyWorkload yearly = new YearlyWorkload(null, 2025, new ArrayList<>(List.of(monthly)));
        TrainerWorkload trainer = new TrainerWorkload(username, "John", "Doe", true, new ArrayList<>(List.of(yearly)));

        db.put(username, trainer);
        when(trainerRepo.findById(username)).thenReturn(Optional.of(trainer));
    }

    @When("a training session event is received with duration {int} for {string} in year {int} and month {int}")
    public void training_event_received(int duration, String username, int year, int month) {
        TrainingSessionEventDTO dto = new TrainingSessionEventDTO();
        dto.setUsername(username);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setActionType("ADD");
        dto.setTrainingDate(LocalDate.of(year, month, 1));
        dto.setTrainingDuration(duration);

        // Simulate saving in memory
        when(trainerRepo.save(any())).thenAnswer(invocation -> {
            TrainerWorkload saved = invocation.getArgument(0);
            db.put(saved.getUsername(), saved);
            return saved;
        });

        workloadService.processTrainingSessionEvent(dto);
    }

    @Then("a new trainer workload should be created with {int} minutes for that month")
    public void verify_created_trainer(int expectedMinutes) {
        TrainerWorkload trainer = db.get("john123");
        assertNotNull(trainer);
        YearlyWorkload yearly = trainer.getYearlyWorkloads().stream()
                .filter(y -> y.getYear() == 2025)
                .findFirst()
                .orElse(null);
        assertNotNull(yearly);
        MonthlyWorkload monthly = yearly.getMonthlyWorkloads().stream()
                .filter(m -> m.getMonth() == 6)
                .findFirst()
                .orElse(null);
        assertNotNull(monthly);
        assertEquals(expectedMinutes, monthly.getTotalTrainingDuration());
    }

    @Then("the trainer workload should be updated to {int} minutes for that month")
    public void verify_updated_trainer(int expectedMinutes) {
        verify_created_trainer(expectedMinutes); // same check
    }

    @When("the monthly summary is requested for {string} in year {int} and month {int}")
    public void request_summary(String username, int year, int month) {
        when(trainerRepo.findById(username)).thenReturn(Optional.of(db.get(username)));
        summaryDTO = workloadService.getMonthlySummary(username, year, month);
    }

    @Then("the returned summary should contain {int} minutes")
    public void assert_summary_minutes(int expectedMinutes) {
        assertNotNull(summaryDTO);
        assertEquals(expectedMinutes, summaryDTO.getTotalTrainingDuration());
    }
}
