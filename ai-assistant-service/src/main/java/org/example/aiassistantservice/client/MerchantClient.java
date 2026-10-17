package org.example.aiassistantservice.client;

import org.example.aiassistantservice.dto.MerchantDtos;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "merchant-service", url = "${services.merchant.url}")
public interface MerchantClient {

    @GetMapping("/api/merchant/balance")
    BigDecimal getBalance(@RequestHeader("X-Auth-User-Id") UUID userId, @RequestBody MerchantDtos.MerchantBalanceRequest request);

    @PostMapping("/api/merchant/create-merchant")
    String createMerchant(@RequestHeader("X-Auth-User-Id") UUID userId, @RequestBody MerchantDtos.CreateMerchantRequest request);

}
