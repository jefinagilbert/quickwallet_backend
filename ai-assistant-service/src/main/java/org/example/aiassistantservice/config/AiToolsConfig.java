package org.example.aiassistantservice.config;

import feign.FeignException;
import org.example.aiassistantservice.client.MerchantClient;
import org.example.aiassistantservice.client.WalletClient;
import org.example.aiassistantservice.context.UserContext;
import org.example.aiassistantservice.dto.AiToolsDtos.*;
import org.example.aiassistantservice.dto.MerchantDtos;
import org.example.aiassistantservice.dto.WalletbalanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Function;

@Configuration
public class AiToolsConfig {

    private static final Logger log = LoggerFactory.getLogger(AiToolsConfig.class);

    private final WalletClient walletClient;
    private final MerchantClient merchantClient;

    public AiToolsConfig(WalletClient walletClient, MerchantClient merchantClient) {
        this.walletClient = walletClient;
        this.merchantClient = merchantClient;
    }

    @Bean
    @Description("Gets the balance of a specific user wallet")
    public Function<EmptyInput, WalletBalanceOutput> getWalletBalanceFunction() {
        return request -> {
            UUID userId = UserContext.getUserId();
            if (userId == null) {
                return new WalletBalanceOutput("UNAUTHORIZED", null, "User is not authenticated.");
            }

            try {
                log.info("AI calling walletClient.getWalletBalance for userId: {}", userId);
                BigDecimal balance = walletClient.getWalletBalance(userId);
                return new WalletBalanceOutput("SUCCESS", balance, "The current balance for wallet" + " is " + balance);
            } catch (Exception e) {
                log.error("Error fetching wallet balance: {}", e.getMessage());
                return new WalletBalanceOutput("ERROR", null, "Unable to fetch wallet balance: " + e.getMessage());
            }
        };
    }

    @Bean
    @Description("Registers a new merchant account. Requires a business name. Use only when businessName is provided.")
    public Function<CreateMerchantInput, CreateMerchantOutput> createMerchantFunction() {
        return input -> {
            UUID userId = UserContext.getUserId();
            if (userId == null) {
                return new CreateMerchantOutput("UNAUTHORIZED", "User is not authenticated.");
            }
            if (input == null || input.businessName() == null || input.businessName().isBlank()) {
                return new CreateMerchantOutput("MISSING_ARGUMENT", "Business name is missing. Ask user what business name they want to use.");
            }

            try {
                log.info("AI calling merchantClient.createMerchant for userId: {}, businessName: {}", userId, input.businessName());
                String result = merchantClient.createMerchant(userId, new MerchantDtos.CreateMerchantRequest(input.businessName().trim()));
                return new CreateMerchantOutput("SUCCESS", result != null ? result : "Merchant account created successfully.");
            } catch (Exception e) {
                log.error("Error creating merchant account: {}", e.getMessage());
                return new CreateMerchantOutput("ERROR", "Unable to create merchant account: " + e.getMessage());
            }
        };
    }

    @Bean
    @Description("Gets the balance of a specific merchant account. Use only when merchantId is provided.")
    public Function<MerchantBalanceInput, MerchantBalanceOutput> getMerchantBalanceFunction() {
        return input -> {
            UUID userId = UserContext.getUserId();
            if (userId == null) {
                return new MerchantBalanceOutput("UNAUTHORIZED", null, "User is not authenticated.");
            }
            if (input == null || input.merchantId() == null || input.merchantId().isBlank()) {
                return new MerchantBalanceOutput("MISSING_ARGUMENT", null, "Merchant ID is missing. Ask user for merchantId.");
            }

            try {
                log.info("AI calling merchantClient.getBalance for userId: {}, merchantId: {}", userId, input.merchantId());
                BigDecimal balance = merchantClient.getBalance(userId, new MerchantDtos.MerchantBalanceRequest(input.merchantId().trim()));
                return new MerchantBalanceOutput("SUCCESS", balance, "The current balance for merchant " + input.merchantId() + " is " + balance);
            } catch (FeignException.NotFound e) {
                return new MerchantBalanceOutput("NOT_FOUND", null, "No merchant found with ID: " + input.merchantId());
            } catch (Exception e) {
                log.error("Error fetching merchant balance: {}", e.getMessage());
                return new MerchantBalanceOutput("ERROR", null, "Unable to fetch merchant balance: " + e.getMessage());
            }
        };
    }
}
