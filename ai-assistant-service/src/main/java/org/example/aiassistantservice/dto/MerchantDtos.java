package org.example.aiassistantservice.dto;

public class MerchantDtos {
    public record MerchantBalanceRequest(String merchantId) {}
    public record CreateMerchantRequest(String businessName) {}
}
