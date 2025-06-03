package com.epam.microservices.TrainingSummary.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    private String username;

    private String firstName;
    private String lastName;
    private boolean isActive;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "trainer_username")
    private List<YearlyWorkload> yearlyWorkloads = new ArrayList<>();

    public TrainerWorkload(String username, String firstName, String lastName, boolean active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = active;
    }

    public TrainerWorkload() {

    }
}
