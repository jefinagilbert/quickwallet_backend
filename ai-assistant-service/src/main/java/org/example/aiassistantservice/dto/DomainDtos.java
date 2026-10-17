package org.example.aiassistantservice.dto;

import java.math.BigDecimal;

public class DomainDtos {
    public record BalanceResponse(BigDecimal balance) {}

    public record MerchantRegisterRequest(String userId, String businessName, String category) {}
    public record MerchantResponse(String merchantId, String businessName, String status) {}

    public record PaymentDraftRequest(String senderId, String recipientIdentifier, BigDecimal amount) {}
    public record PaymentDraftResponse(String draftId, String recipient, BigDecimal amount, String status) {}
}
