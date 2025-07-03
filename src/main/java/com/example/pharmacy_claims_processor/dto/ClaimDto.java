package com.example.pharmacy_claims_processor.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClaimDto {
    private String patientId;
    private String pharmacyId;
    private String insurancePolicyNumber;
    private BigDecimal claimCost;
    private BigDecimal insuranceCoverage;
}
