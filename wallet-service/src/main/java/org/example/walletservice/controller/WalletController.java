package org.example.walletservice.controller;
import org.example.walletservice.dto.AddFundRequest;
import org.example.walletservice.dto.NewWalletRequest;
import org.example.walletservice.dto.WalletBalanceRequest;
import org.example.walletservice.entity.WalletEntity;
import org.example.walletservice.repository.WalletRepository;
import org.example.walletservice.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletRepository walletRepository;
    private final WalletService walletService;

    public WalletController(WalletRepository walletRepository, WalletService walletService) {
        this.walletRepository = walletRepository;
        this.walletService = walletService;
    }

    @PostMapping("/create-wallet")
    public ResponseEntity<String> createWallet(@RequestHeader("X-Auth-User-Id") UUID userId, @RequestBody NewWalletRequest request) {
        walletService.initializeWallet(userId, request.name());
        return ResponseEntity.ok("Created Successfully");
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getWalletBalance(@RequestHeader("X-Auth-User-Id") UUID userId, @RequestBody WalletBalanceRequest request) {
        return ResponseEntity.ok(walletService.getBalance(userId, request.walletId()));
    }

    @PostMapping("/addFunds")
    public ResponseEntity<String> addFunds(@RequestHeader("X-Auth-User-Id") UUID userId, @RequestBody AddFundRequest request) {
        walletService.addFunds(userId, request.id(), request.amount());
        return ResponseEntity.ok("Successfully added " + request.amount());
    }

    @PostMapping("/internal/debit")
    public ResponseEntity<String> debitWallet(
            @RequestParam("userId") UUID userId,
            @RequestParam("walletId") String walletId,
            @RequestParam("amount") BigDecimal amount) {
        walletService.debitFunds(userId, walletId, amount);
        return ResponseEntity.ok("SOURCE_DEBITED");
    }

    @PostMapping("/internal/credit")
    public ResponseEntity<String> creditWallet(
            @RequestParam("userId") UUID userId,
            @RequestParam("walletId") String walletId,
            @RequestParam("amount") BigDecimal amount) {
        walletService.creditFunds(userId, walletId, amount);
        return ResponseEntity.ok("DESTINATION_CREDITED");
    }

    @PostMapping("/internal/refund")
    public ResponseEntity<String> refundWallet(
            @RequestParam("userId") UUID userId,
            @RequestParam("walletId") String walletId,
            @RequestParam("amount") BigDecimal amount) {
        walletService.refundFunds(userId, walletId, amount);
        return ResponseEntity.ok("ROLLED_BACK");
    }
}
