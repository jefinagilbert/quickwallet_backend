package org.example.aiassistantservice.client;

import org.example.aiassistantservice.dto.DomainDtos;
import org.example.aiassistantservice.dto.WalletbalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "wallet-service", url = "${services.wallet.url}")
public interface WalletClient {
    @GetMapping("/api/wallet/balance")
    BigDecimal getWalletBalance(@RequestHeader("X-Auth-User-Id") UUID userId);
}
