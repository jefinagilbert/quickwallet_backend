package org.example.paymentorchestratorservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient("merchant-service")
public interface MerchantClient {

    @PostMapping("/api/merchant/internal/credit")
    String creditMerchant(@RequestParam("userId") UUID userId, @RequestParam("merchantId") String merchantId, @RequestParam("amount") BigDecimal amount);
}
