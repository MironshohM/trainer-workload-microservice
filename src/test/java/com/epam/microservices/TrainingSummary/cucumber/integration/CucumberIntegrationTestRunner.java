package com.epam.microservices.TrainingSummary.cucumber.integration;



import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features/integration",
        glue = "com.yourapp.integration",
        plugin = {"pretty", "html:target/cucumber-integration-report.html"},
        monochrome = true
)
public class CucumberIntegrationTestRunner {
}
