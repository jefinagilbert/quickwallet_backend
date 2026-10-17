package org.example.aiassistantservice.service;

import org.example.aiassistantservice.context.UserContext;
import org.example.aiassistantservice.dto.ChatDtos.ChatRequest;
import org.example.aiassistantservice.dto.ChatDtos.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ChatResponse chat(UUID userId, ChatRequest request) {
        String conversationId = request.conversationId();
        if (conversationId == null || conversationId.isBlank()) {
            conversationId = "conv-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        try {
            UserContext.setUserId(userId);
            log.info("Processing chat for userId: {}, conversationId: {}", userId, conversationId);

            final String activeConversationId = conversationId;
            String assistantReply = chatClient.prompt()
                    .user(request.message())
                    .advisors(advisorSpec -> advisorSpec.param(
                            MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,
                            activeConversationId
                    ))
                    .call()
                    .content();

            if (assistantReply == null || assistantReply.isBlank()) {
                assistantReply = "I have processed your request, but have no additional information to provide.";
            }

            return new ChatResponse(assistantReply, activeConversationId);
        } catch (Exception e) {
            log.error("Error during AI chat processing for userId: {}, conversationId: {}. Cause: {}",
                    userId, conversationId, e.getMessage(), e);
            throw e;
        } finally {
            UserContext.clear();
        }
    }
}
