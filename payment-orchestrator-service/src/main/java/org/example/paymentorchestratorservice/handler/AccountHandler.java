package org.example.paymentorchestratorservice.handler;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountHandler {
    boolean supports(String accountId);
    void debit(UUID userId, String sourceId, BigDecimal amount);
    void credit(UUID userId, String destinationId, BigDecimal amount);
}
