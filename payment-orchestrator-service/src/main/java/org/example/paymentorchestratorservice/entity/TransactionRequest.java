package org.example.paymentorchestratorservice.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionRequest(String sourceId, String destinationId, UUID destinationUserId, BigDecimal amount) {
}
