package com.epam.microservices.TrainingSummary.cucumber.integration;

import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryDTO;
import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryRequest;
import com.epam.microservices.TrainingSummary.dtos.TrainingSessionEventDTO;
import com.epam.microservices.TrainingSummary.model.MonthlyWorkload;
import com.epam.microservices.TrainingSummary.model.TrainerWorkload;
import com.epam.microservices.TrainingSummary.model.YearlyWorkload;
import com.epam.microservices.TrainingSummary.repository.TrainerWorkloadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test") // Optional: can be used for test-specific configs
public class ActiveMQIntegrationStepDefinitions {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private TrainerWorkloadRepository trainerRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private final String trainingSessionQueue = "training-session-events";
    private final String summaryRequestQueue = "monthly.summary.request.queue";

    private final String testUsername = "john123";
    private MonthlySummaryDTO receivedSummary;

    @Before
    public void setup() {
        trainerRepo.deleteAll(); // Clear DB before each scenario
    }

    @Given("the trainer {string} does not exist in MongoDB")
    public void trainer_does_not_exist(String username) {
        trainerRepo.deleteById(username);
    }

    @Given("the trainer {string} exists with {int} minutes in June 2025")
    public void trainer_exists_with_data(String username, int minutes) {
        MonthlyWorkload monthly = new MonthlyWorkload(null, 6, minutes);
        YearlyWorkload yearly = new YearlyWorkload(null, 2025, List.of(monthly));
        TrainerWorkload trainer = new TrainerWorkload(username, "John", "Doe", true, List.of(yearly));
        trainerRepo.save(trainer);
    }

    @When("a training session event with {int} minutes is sent to ActiveMQ for {string}")
    public void send_training_event(int duration, String username) {
        TrainingSessionEventDTO dto = new TrainingSessionEventDTO();
        dto.setUsername(username);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDuration(duration);
        dto.setTrainingDate(LocalDate.of(2025, 6, 1));
        dto.setActionType("ADD");

        jmsTemplate.convertAndSend(trainingSessionQueue, dto);
        waitForProcessing(); // optional delay to allow message to process
    }

    @When("a summary request is sent to ActiveMQ for {string} in year {int} and month {int}")
    public void send_summary_request(String username, int year, int month) throws Exception {
        MonthlySummaryRequest request = new MonthlySummaryRequest();
        request.setUsername(username);
        request.setYear(year);
        request.setMonth(month);

        Destination replyQueue = jmsTemplate.getConnectionFactory()
                .createConnection()
                .createSession(false, Session.AUTO_ACKNOWLEDGE)
                .createTemporaryQueue();

        jmsTemplate.send(summaryRequestQueue, session -> {
            Message message = session.createObjectMessage(request);
            message.setJMSReplyTo(replyQueue);
            return message;
        });

        Object reply = jmsTemplate.receiveAndConvert(replyQueue);
        receivedSummary = objectMapper.convertValue(reply, MonthlySummaryDTO.class);
    }

    @Then("a new trainer workload should be created in MongoDB with {int} minutes")
    public void verify_trainer_created(int expected) {
        TrainerWorkload trainer = trainerRepo.findById(testUsername).orElseThrow();
        YearlyWorkload yearly = trainer.getYearlyWorkloads().stream()
                .filter(y -> y.getYear() == 2025)
                .findFirst().orElseThrow();
        MonthlyWorkload monthly = yearly.getMonthlyWorkloads().stream()
                .filter(m -> m.getMonth() == 6)
                .findFirst().orElseThrow();
        Assertions.assertEquals(expected, monthly.getTotalTrainingDuration());
    }

    @Then("the response should contain {int} minutes")
    public void verify_response_summary(int expected) {
        Assertions.assertNotNull(receivedSummary);
        Assertions.assertEquals(expected, receivedSummary.getTotalTrainingDuration());
    }

    private void waitForProcessing() {
        try {
            Thread.sleep(500); // wait for JMS listener to process
        } catch (InterruptedException ignored) {}
    }
}