package com.example.pharmacy_claims_processor.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.pharmacy_claims_processor.model.Claim;
import com.example.pharmacy_claims_processor.model.ClaimStatus;
import com.example.pharmacy_claims_processor.repository.ClaimRepository;

@Service
public class ClaimProcessorService {
    
    public static final Logger log = LoggerFactory.getLogger(ClaimProcessorService.class);

    private final ClaimRepository claimRepository;
    private final ProcessedClaimProducer processedClaimProducer;
    private final ClaimRetryHandler claimRetryHandler;

    public ClaimProcessorService (ClaimRepository claimRepository, ProcessedClaimProducer processedClaimProducer, ClaimRetryHandler claimRetryHandler) {
        this.claimRepository = claimRepository;
        this. processedClaimProducer = processedClaimProducer;
        this.claimRetryHandler = claimRetryHandler;
    }

    public void processClaim(Claim claim) {
        try {
            claimRepository.save(claim);
            log.info("Claim received and persisted with ID: {}", claim.getId());

            applyBusinessRules(claim);
            log.info("Business rules validation passed for claim ID: {}", claim.getId());

            claim.setStatus(ClaimStatus.PROCESSED);
            claim.setProcessedAt(LocalDateTime.now());
            Claim processedClaim = claimRepository.save(claim);
            log.info("Claim {} successfully processed and status updated.", processedClaim.getId());

            processedClaimProducer.send(processedClaim);
        } catch (Exception e) {
            log.error("An error occured while processing claim ID {}: {}", claim.getId(), e.getMessage());
            claimRetryHandler.handleFailure(claim, e);
        }
    }

    private void applyBusinessRules(Claim claim) {
        if (claim.getClaimCost() == null || claim.getClaimCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid claim cost: cannot be null or negative.");
        }

        if (claim.getPatientId() == null || claim.getPatientId().isBlank()) {
            throw new IllegalArgumentException("Invalid claim: Patient ID is missing.");
        }

        if (claim.getPharmacyId() == null || claim.getPharmacyId().isBlank()) {
            throw new IllegalArgumentException("Invalid claim: Pharmacy ID is missing.");
        }
    }
}
