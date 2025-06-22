package com.epam.microservices.TrainingSummary.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "trainer_workloads")
@CompoundIndexes({
        @CompoundIndex(name = "first_last_name_idx", def = "{'firstName' : 1, 'lastName' : 1}")
})
public class TrainerWorkload {

    @Id
    private String username;

    private String firstName;
    private String lastName;
    private boolean isActive;

    private List<YearlyWorkload> yearlyWorkloads = new ArrayList<>();

    public TrainerWorkload(String username, String firstName, String lastName, boolean active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = active;
    }
}
