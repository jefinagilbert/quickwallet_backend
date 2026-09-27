package org.example.paymentorchestratorservice.handler;
import org.example.paymentorchestratorservice.service.WalletClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class WalletHandler implements AccountHandler {

    private final WalletClient walletClient;

    public WalletHandler(WalletClient walletClient) {
        this.walletClient = walletClient;
    }

    @Override
    public boolean supports(String accountId) {
        return accountId != null && accountId.startsWith("wal_");
    }

    @Override
    public void debit(UUID userId, String sourceId, BigDecimal amount) {
        walletClient.debitWallet(userId, sourceId, amount);
    }

    @Override
    public void credit(UUID userId, String destinationId, BigDecimal amount) {
        walletClient.creditWallet(userId, destinationId, amount);
    }

}
