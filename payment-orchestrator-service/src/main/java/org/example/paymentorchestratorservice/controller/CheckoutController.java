package org.example.paymentorchestratorservice.controller;

import org.example.paymentorchestratorservice.dto.CheckoutRequest;
import org.example.paymentorchestratorservice.dto.P2pRequest;
import org.example.paymentorchestratorservice.entity.TransactionRequest;
import org.example.paymentorchestratorservice.entity.TransactionState;
import org.example.paymentorchestratorservice.entity.Transactions;
import org.example.paymentorchestratorservice.service.SagaOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transfer")
public class CheckoutController {

    private final SagaOrchestratorService sagaOrchestratorService;

    public CheckoutController(SagaOrchestratorService sagaOrchestratorService) {
        this.sagaOrchestratorService = sagaOrchestratorService;
    }

    @PostMapping("/send-money")
    public ResponseEntity<String> sendMoney(
            @RequestHeader("X-Auth-User-Id") UUID userId,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestBody TransactionRequest request
            ) {
        sagaOrchestratorService.sendMoney(userId, idempotencyKey, request);
        return ResponseEntity.ok("Success");
    }
}
