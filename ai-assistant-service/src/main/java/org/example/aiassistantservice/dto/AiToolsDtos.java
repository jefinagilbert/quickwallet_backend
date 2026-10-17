package org.example.aiassistantservice.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.math.BigDecimal;

public class AiToolsDtos {

    public record EmptyInput() {}

    public record WalletBalanceInput(
            @JsonPropertyDescription("The ID of the wallet to check (e.g. wal_123). Required.")
            String walletId
    ) {}

    public record WalletBalanceOutput(
            String status,
            BigDecimal balance,
            String message
    ) {}

    public record CreateMerchantInput(
            @JsonPropertyDescription("The legal or trading business name for the new merchant account. Required.")
            String businessName
    ) {}

    public record CreateMerchantOutput(
            String status,
            String message
    ) {}

    public record MerchantBalanceInput(
            @JsonPropertyDescription("The ID of the merchant to check (e.g. mer_123). Required.")
            String merchantId
    ) {}

    public record MerchantBalanceOutput(
            String status,
            BigDecimal balance,
            String message
    ) {}
}
