package org.example.walletservice.dto;

import java.math.BigDecimal;

public record AddFundRequest(String id, BigDecimal amount) {
}
