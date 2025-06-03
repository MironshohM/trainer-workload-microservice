package com.epam.microservices.TrainingSummary.controller;



import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryDTO;
import com.epam.microservices.TrainingSummary.dtos.TrainingSessionEventDTO;
import com.epam.microservices.TrainingSummary.service.TrainingWorkloadService;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TrainingWorkloadController.class)
@Import(TrainingWorkloadControllerTest.MockServiceConfig.class)
class TrainingWorkloadControllerTest {

    private static final TrainingWorkloadService workloadService = Mockito.mock(TrainingWorkloadService.class);

    @TestConfiguration
    static class MockServiceConfig {
        @Bean
        public TrainingWorkloadService trainingWorkloadService() {
            return workloadService;
        }
    }

    @javax.annotation.Resource
    private MockMvc mockMvc;

    @javax.annotation.Resource
    private ObjectMapper objectMapper;

    private TrainingSessionEventDTO sessionEventDTO;
    private MonthlySummaryDTO summaryDTO;

    @BeforeEach
    void setup() {
        sessionEventDTO = new TrainingSessionEventDTO();
        sessionEventDTO.setUsername("john");
        sessionEventDTO.setFirstName("John");
        sessionEventDTO.setLastName("Doe");
        sessionEventDTO.setActive(true);
        sessionEventDTO.setTrainingDuration(60);
        sessionEventDTO.setTrainingDate(LocalDate.of(2024, 5, 15));
        sessionEventDTO.setActionType("ADD");

        summaryDTO = new MonthlySummaryDTO("john", "John", "Doe", true, 2024, 5, 180);
    }

    @Test
    void testUpdateWorkload_Success() throws Exception {
        mockMvc.perform(post("/api/workload/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionEventDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Training workload updated successfully"));

        Mockito.verify(workloadService).processTrainingSessionEvent(any(TrainingSessionEventDTO.class));
    }

    @Test
    void testUpdateWorkload_Failure() throws Exception {
        Mockito.doThrow(new RuntimeException("Database error"))
                .when(workloadService).processTrainingSessionEvent(any());

        mockMvc.perform(post("/api/workload/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionEventDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to update training workload"));
    }

    @Test
    void testGetMonthlySummary_Success() throws Exception {
        Mockito.when(workloadService.getMonthlySummary(eq("john"), eq(2024), eq(5)))
                .thenReturn(summaryDTO);

        mockMvc.perform(get("/api/workload/john/2024/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.totalTrainingDuration").value(180));
    }

    @Test
    void testGetMonthlySummary_Failure() throws Exception {
        Mockito.when(workloadService.getMonthlySummary(eq("john"), eq(2024), eq(5)))
                .thenThrow(new RuntimeException("Trainer not found"));

        mockMvc.perform(get("/api/workload/john/2024/5"))
                .andExpect(status().isInternalServerError());
    }
}
