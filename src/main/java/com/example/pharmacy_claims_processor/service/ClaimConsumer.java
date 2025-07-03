package com.example.pharmacy_claims_processor.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.example.pharmacy_claims_processor.dto.ClaimDto;
import com.example.pharmacy_claims_processor.model.Claim;
import com.example.pharmacy_claims_processor.model.ClaimStatus;

@Service
public class ClaimConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(ClaimConsumer.class);
    private final ClaimProcessorService processorService;

    public ClaimConsumer(ClaimProcessorService processorService) {
        this.processorService = processorService;
    }

    // @KafkaListener(topics = "${app.kafka.topic.claims}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    // public void handleClaim(@Payload ClaimDto claimDto) {
    //     log.info("Received new claim from Kafka fo patient: {}", claimDto.getPatientId());

    //     Claim claim = mapDtoToEntity(claimDto);

    //     processorService.processClaim(claim);
    // }

    public void handleNewClaim(@Payload ClaimDto claimDto) {
        log.info("Received new claim from Kafka for patient: {}", claimDto.getPatientId());
        
        Claim claim = mapDtoToEntity(claimDto);
        processorService.processClaim(claim);
    }

    @KafkaListener(topics = "${app.kafka.topic.retry}", groupId = "${spring.kafka.consumer.group-id}",
                   containerFactory = "kafkaListenerContainerFactory") // Ensure Claim entity deserialization
    public void handleRetryClaim(@Payload Claim claim) {
        log.info("Received claim ID {} from retry topic. Re-processing...", claim.getId());
        
        processorService.processClaim(claim);
    }

    private Claim mapDtoToEntity(ClaimDto dto) {
        return Claim.builder()
            .patientId(dto.getPatientId())
            .pharmacyId(dto.getPharmacyId())
            .insurancePolicyNumber(dto.getInsurancePolicyNumber())
            .claimCost(dto.getClaimCost())
            .insuranceCoverage(dto.getInsuranceCoverage())
            .status(ClaimStatus.RECEIVED)
            .retryCount(0)
            .submittedAt(LocalDateTime.now())
            .build();
    }
}
