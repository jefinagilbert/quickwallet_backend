package org.example.paymentorchestratorservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record P2pRequest(UUID walletId, BigDecimal amount) {
}
