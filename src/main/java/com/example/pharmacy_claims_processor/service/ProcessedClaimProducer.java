package com.example.pharmacy_claims_processor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import com.example.pharmacy_claims_processor.model.Claim;

public class ProcessedClaimProducer {
    private static final Logger log = LoggerFactory.getLogger(ProcessedClaimProducer.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String processedTopic;

    public ProcessedClaimProducer(KafkaTemplate<String, Object> kafkaTemplate, @Value("${app.kafka.topic.processed}") String processedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.processedTopic = processedTopic;
    }

    public void send(Claim processedClaim) {
        log.info("Publishing processed claim {} to  topic '{}", processedClaim.getId(), processedTopic);

        kafkaTemplate.send(processedTopic, String.valueOf(processedClaim.getId()), processedClaim).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send processed claim {} to Kafka", processedClaim.getId(), ex);
            } else {
                log.info("Successfully sent processed claim {} to offset {}", processedClaim.getId(), result.getRecordMetadata().offset());
            }
        });
    }
}
