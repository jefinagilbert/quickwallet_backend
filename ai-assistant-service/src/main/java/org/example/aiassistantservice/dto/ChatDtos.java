package org.example.aiassistantservice.dto;

public class ChatDtos {

    public record ChatRequest(
            String message,
            String conversationId
    ) {}

    public record ChatResponse(
            String response,
            String conversationId
    ) {}
}
