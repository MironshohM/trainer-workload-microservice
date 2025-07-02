Feature: Trainer Workload Management

  Scenario: Add training session for new trainer
    Given the trainer "john123" does not exist in the database
    When a training session event is received with duration 60 for "john123" in year 2025 and month 6
    Then a new trainer workload should be created with 60 minutes for that month

  Scenario: Add training session for existing trainer
    Given the trainer "john123" exists with 60 minutes in June 2025
    When a training session event is received with duration 30 for "john123" in year 2025 and month 6
    Then the trainer workload should be updated to 90 minutes for that month

  Scenario: Fetch monthly summary for trainer
    Given the trainer "john123" exists with 90 minutes in June 2025
    When the monthly summary is requested for "john123" in year 2025 and month 6
    Then the returned summary should contain 90 minutes
