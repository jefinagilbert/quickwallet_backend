package org.example.paymentorchestratorservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutRequest(UUID merchantId, BigDecimal amount) {

}
