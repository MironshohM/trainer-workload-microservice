package com.epam.microservices.TrainingSummary.cucumber;


import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",     // path to .feature files
        glue = "com.yourapp.training.summary.cucumber", // package where step defs are
        plugin = {"pretty", "html:target/cucumber-report.html"},
        monochrome = true
)
public class CucumberTestRunner {

}
