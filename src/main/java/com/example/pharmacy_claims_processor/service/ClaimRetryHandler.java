package com.example.pharmacy_claims_processor.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.pharmacy_claims_processor.model.Claim;
import com.example.pharmacy_claims_processor.model.ClaimStatus;
import com.example.pharmacy_claims_processor.model.ErrorClaim;
import com.example.pharmacy_claims_processor.repository.ClaimRepository;
import com.example.pharmacy_claims_processor.repository.ErrorClaimRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ClaimRetryHandler {
    private static final Logger log = LoggerFactory.getLogger(ClaimRetryHandler.class);

    private final ClaimRepository claimRepository;
    private final ErrorClaimRepository errorClaimRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final int maxRetries;
    private final String retryTopic;

    public ClaimRetryHandler(ClaimRepository claimRepository, ErrorClaimRepository errorClaimRepository, KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper, @Value("${app.kafka.retry-count}") int maxRetries, @Value("${app.kafka.topic.retry}") String retryTopic) {
        this.claimRepository = claimRepository;
        this.errorClaimRepository = errorClaimRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.maxRetries = maxRetries;
        this.retryTopic = retryTopic;
    }

    public void handleFailure(Claim claim, Exception exception) {
        if (claim.getRetryCount() < maxRetries) {
            retryClaim(claim);
        } else {
            persistAsError(claim, exception);
        }
    }

    private void retryClaim(Claim claim) {
        claim.setRetryCount(claim.getRetryCount() + 1);
        claim.setStatus(ClaimStatus.ERROR);
        claimRepository.save(claim);

        log.info("Preparing to retry claim ID {}. Attempt {}/{}", claim.getId(), claim.getRetryCount(), maxRetries);

        kafkaTemplate.send(retryTopic, String.valueOf(claim.getId()), claim);
    }

    private void persistAsError(Claim claim, Exception exception) {
        log.warn("Max retries ({}) reached for claim ID {}. Moving to error table.", maxRetries, claim.getId());

        claim.setStatus(ClaimStatus.ERROR);
        claimRepository.save(claim);

        try {
            ErrorClaim errorClaim = ErrorClaim.builder()
                .failedClaimPayload(objectMapper.writeValueAsString(claim))
                .errorMessage(exception.getMessage())
                .errorTimestamp(LocalDateTime.now())
                .build();
            errorClaimRepository.save(errorClaim);
            log.info("Claim ID {} successfully logged to error_claims table.", claim.getId());
        } catch (JsonProcessingException e) {
            log.error("Could not serialize failed claim ID {} to JSON for error logging.", claim.getId(), e);
        }
    }
}
