package com.epam.microservices.TrainingSummary.controller;

import com.epam.microservices.TrainingSummary.dtos.MonthlySummaryDTO;
import com.epam.microservices.TrainingSummary.dtos.TrainingSessionEventDTO;
import com.epam.microservices.TrainingSummary.service.TrainingWorkloadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

@RestController
@RequestMapping("/api/workload")
@RequiredArgsConstructor
public class TrainingWorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingWorkloadController.class);

    private final TrainingWorkloadService workloadService;


    // Accept training session event (ADD or DELETE)
    @Operation(
            summary = "Update trainer workload",
            description = "Processes a training session event (ADD or DELETE) to update a trainer's workload."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training workload updated successfully",
                    content = @Content(mediaType = "text/plain")),
            @ApiResponse(responseCode = "500", description = "Failed to update training workload",
                    content = @Content(mediaType = "text/plain"))
    })
    @PostMapping("/update")
    public ResponseEntity<String> updateWorkload(@RequestBody TrainingSessionEventDTO dto) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        logger.info("TransactionID: {} - Received training session event: {}", transactionId, dto);

        try {
            workloadService.processTrainingSessionEvent(dto);
            logger.info("TransactionID: {} - Training workload processed successfully", transactionId);
            return ResponseEntity.ok("Training workload updated successfully");
        } catch (Exception e) {
            logger.error("TransactionID: {} - Error while processing training session event: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to update training workload");
        } finally {
            MDC.clear();
        }
    }

    // Retrieve summary for a specific trainer in a specific month
    @Operation(
            summary = "Get monthly workload summary",
            description = "Retrieves the total training duration for a trainer in a given month."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved monthly summary",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MonthlySummaryDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error while retrieving summary",
                    content = @Content(mediaType = "text/plain"))
    })
    @GetMapping("/{username}/{year}/{month}")
    public ResponseEntity<MonthlySummaryDTO> getMonthlySummary(
            @PathVariable String username,
            @PathVariable int year,
            @PathVariable int month) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put("transactionId", transactionId);
        logger.info("TransactionID: {} - Request to get workload summary for trainer: {}, year: {}, month: {}",
                transactionId, username, year, month);

        try {
            MonthlySummaryDTO summary = workloadService.getMonthlySummary(username, year, month);
            logger.info("TransactionID: {} - Successfully retrieved summary", transactionId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("TransactionID: {} - Error while retrieving monthly summary: {}", transactionId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        } finally {
            MDC.clear();
        }
    }
}
