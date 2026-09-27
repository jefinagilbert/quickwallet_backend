package org.example.paymentorchestratorservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @PostMapping("/api/wallet/internal/debit")
    String debitWallet(@RequestParam("userId") UUID userId, @RequestParam("walletId") String walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/api/wallet/internal/refund")
    String refundWallet(@RequestParam("userId") UUID userId, @RequestParam("walletId") String walletId, @RequestParam("amount") BigDecimal amount);

    @PostMapping("/api/wallet/internal/credit")
    String creditWallet(@RequestParam("userId") UUID userId, @RequestParam("walletId") String walletId, @RequestParam("amount") BigDecimal amount);
}
