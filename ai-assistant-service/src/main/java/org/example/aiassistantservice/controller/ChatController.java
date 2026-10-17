package org.example.aiassistantservice.controller;

import org.example.aiassistantservice.dto.ChatDtos.ChatRequest;
import org.example.aiassistantservice.dto.ChatDtos.ChatResponse;
import org.example.aiassistantservice.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestHeader(value = "X-Auth-User-Id", required = false) UUID userId,
            @RequestBody ChatRequest request) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ChatResponse("Unauthorized: Missing X-Auth-User-Id header.", null));
        }

        if (request == null || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Message cannot be empty.", null));
        }

        try {
            ChatResponse response = chatService.chat(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("Failed to process message: " + e.getMessage(), request.conversationId()));
        }
    }
}
