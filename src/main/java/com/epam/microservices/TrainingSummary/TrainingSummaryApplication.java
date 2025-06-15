package com.epam.microservices.TrainingSummary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class TrainingSummaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainingSummaryApplication.class, args);
	}

}
