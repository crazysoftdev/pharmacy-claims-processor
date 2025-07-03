package com.example.pharmacy_claims_processor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.pharmacy_claims_processor.model.ClaimStatus;
import com.example.pharmacy_claims_processor.repository.ClaimRepository;

@Service
public class ReportingService {
    private static final Logger log = LoggerFactory.getLogger(ReportingService.class);
    private final ClaimRepository claimRepository;

    public ReportingService(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    // Daily Summary Report
    @Scheduled(cron = "0 0 1 * * ?")    // Report at 1.00 AM daily
    public void generateDailySummaryReport() {
        long receivedCount = claimRepository.count();
        // Create a custom query in a real app.
        long processsedCount = claimRepository.findAll().stream()
            .filter(c -> c.getStatus() == ClaimStatus.PROCESSED).count();

        log.info("--- Daily Summary Report ---");
        log.info("Total Claims Received: {}", receivedCount);
        log.info("Total Claims Processed Successfully: {}", processsedCount);
        log.info("--- End of Report ---");
    }

    // Invoice-Style Report
    @Scheduled(fixedRate = 300000)
    public void generateInvoiceReport() {
        log.info("--- Invoice Report ---");
        log.info("This is a placeholder for the pharmacy payment report.");
        // Create a custom query for full implementation
        log.info("--- End of Report ---");
    }
}
