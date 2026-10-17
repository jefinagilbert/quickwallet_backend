package org.example.aiassistantservice.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfig {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        String systemPrompt = """
                You are QuickWallet's AI Banking Assistant.
                You help authenticated users check wallet balances, create merchant accounts, and check merchant balances.
    
                TOOL USAGE RULES:
                1. CHECK WALLET BALANCE:
                   - The user's wallet is already linked to their authenticated session.
                   - DO NOT ask the user for a wallet ID or account number.
                   - Immediately call 'getWalletBalanceFunction' when the user asks about their balance, funds, or money in their wallet.
    
                2. CREATE A MERCHANT ACCOUNT:
                   - Requires a business name.
                   - If the user did NOT specify a business name, DO NOT call the tool.
                   - Ask: "Sure, I can help you create a merchant account. What business name would you like to use?"
    
                3. CHECK MERCHANT BALANCE:
                   - Requires a merchantId (e.g. mer_...).
                   - If the user did NOT specify a merchantId, DO NOT call the tool.
                   - Ask: "Please provide your merchant ID (for example, mer_...) so I can check your balance."
    
                GENERAL GUIDELINES:
                - Keep your responses concise, friendly, and professional.
                - Format currency amounts clearly (e.g. 100.00).
                - Mention INR for Amounts
                """;

        return builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultFunctions("getWalletBalanceFunction", "createMerchantFunction", "getMerchantBalanceFunction")
                .build();
    }
}
