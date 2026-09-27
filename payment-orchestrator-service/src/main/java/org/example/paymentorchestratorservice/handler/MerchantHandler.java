package org.example.paymentorchestratorservice.handler;

import org.example.paymentorchestratorservice.service.MerchantClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MerchantHandler implements AccountHandler {
    private final MerchantClient merchantClient;

    public MerchantHandler(MerchantClient merchantClient) {
        this.merchantClient = merchantClient;
    }

    @Override
    public boolean supports(String accountId) {
        return accountId != null && accountId.startsWith("mer_");
    }

    @Override
    public void debit(UUID userId, String sourceId, BigDecimal amount) {
        throw new UnsupportedOperationException("Debiting a merchant account is not supported yet");
    }

    @Override
    public void credit(UUID userId, String destinationId, BigDecimal amount) {
        merchantClient.creditMerchant(userId, destinationId, amount);
    }
}
