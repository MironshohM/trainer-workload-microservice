package com.epam.microservices.TrainingSummary.repository;

import com.epam.microservices.TrainingSummary.model.TrainerWorkload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerWorkloadRepository extends JpaRepository<TrainerWorkload, String> {

}
