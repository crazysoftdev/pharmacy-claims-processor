package com.example.pharmacy_claims_processor.repository;

import com.example.pharmacy_claims_processor.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    
}
