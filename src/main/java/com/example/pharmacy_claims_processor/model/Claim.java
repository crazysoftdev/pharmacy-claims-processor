package com.example.pharmacy_claims_processor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "claims")

public class Claim {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientId;
    private String pharmacyId;
    private String insurancePolicyNumber;

    @Column(precision = 10, scale = 2)
    private BigDecimal claimCost;

    @Column(precision = 10, scale = 2)
    private BigDecimal insuranceCoverage;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    private int retryCount = 0;

    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
}
