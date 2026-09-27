package org.example.merchantservice.controller;

import org.example.merchantservice.dto.BalanceRequest;
import org.example.merchantservice.dto.NewMerchantRequest;
import org.example.merchantservice.service.MerchantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/create-merchant")
    public ResponseEntity<String> createMerchant(@RequestHeader("X-Auth-User-Id") UUID userId, @RequestBody NewMerchantRequest request) {
        merchantService.createMerchant(userId, request.businessName());
        return ResponseEntity.ok("Merchant Account Created successfully...");
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestHeader("X-Auth-User-Id") UUID userId, @RequestBody BalanceRequest request) {
        return ResponseEntity.ok(merchantService.getMerchantBalance(userId, request.merchantId()));
    }

    @PostMapping("/internal/credit")
    public ResponseEntity<String> creditMerchant(@RequestParam("userId") UUID userId, @RequestParam("merchantId") String merchantId, @RequestParam("amount") BigDecimal amount) {
        merchantService.creditMerchant(userId, merchantId, amount);
        return ResponseEntity.ok("DESTINATION_CREDITED");
    }
}
