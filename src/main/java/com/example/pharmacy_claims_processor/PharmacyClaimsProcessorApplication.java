package com.example.pharmacy_claims_processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;	// For Reporting

@SpringBootApplication
public class PharmacyClaimsProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(PharmacyClaimsProcessorApplication.class, args);
	}

}
