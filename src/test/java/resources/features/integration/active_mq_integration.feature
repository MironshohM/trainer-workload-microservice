Feature: Microservices Integration using ActiveMQ

  Scenario: Fire-and-forget training session event
    Given the trainer "john123" does not exist in MongoDB
    When a training session event with 60 minutes is sent to ActiveMQ for "john123"
    Then a new trainer workload should be created in MongoDB with 60 minutes

  Scenario: Summary request with response
    Given the trainer "john123" exists with 90 minutes in June 2025
    When a summary request is sent to ActiveMQ for "john123" in year 2025 and month 6
    Then the response should contain 90 minutes
