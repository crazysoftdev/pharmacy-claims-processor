package com.example.pharmacy_claims_processor.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.pharmacy_claims_processor.model.Claim;
import com.example.pharmacy_claims_processor.repository.ClaimRepository;

@ExtendWith(MockitoExtension.class)
class ClaimProcessorServiceTest {
    
    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ProcessedClaimProducer processedClaimProducer;

    @Mock
    private ClaimRetryHandler claimRetryHandler;

    @InjectMocks
    private ClaimProcessorService claimProcessorService;

    private Claim validClaim;

    @BeforeEach
    void setUp() {
        // Create a sample valid claim for tests
        validClaim = Claim.builder()
            .id(1L)
            .patientId("PAT123")
            .pharmacyId("PHARM456")
            .claimCost(new BigDecimal("100.00"))
            .build();
    }

    @Test
    void whenClaimIsValid_thenItIsProcessedAndPublished() {
        // Arrange: Define the behavior of the mocks
        // When save is called, return the same claim
        when(claimRepository.save(any(Claim.class))).thenReturn(validClaim);

        // Act: Call the method to test
        claimProcessorService.processClaim(validClaim);

        // Assert: Verify that the expected methods were called
        // Verify it was saved twice (RECEIVED & PROCESSED)
        verify(claimRepository, times(2)).save(validClaim);
        // Verify it was published to the processed topic
        verify(processedClaimProducer, times(1)).send(validClaim);
        // Verify the retry handler was NEVER called
        verify(claimRetryHandler, never()).handleFailure(any(), any());
    }

    @Test
    void whenClaimIsInvalid_thenRetryHandlerIsCalled() {
        // Arrange: Create a claim that will fail business rules
        Claim invalidClaim = Claim.builder().claimCost(BigDecimal.TEN).patientId(null).build();
        when(claimRepository.save(any(Claim.class))).thenReturn(invalidClaim);

        // Act
        claimProcessorService.processClaim(invalidClaim);

        // Assert
        // Verify it was saved once
        verify(claimRepository, times(1)).save(invalidClaim);
        // Verify the producer was NEVER called
        verify(processedClaimProducer, never()).send(any());
        // Verify the retry handler was called
        verify(claimRetryHandler, times(1)).handleFailure(eq(invalidClaim), any(IllegalArgumentException.class));
    }
}
