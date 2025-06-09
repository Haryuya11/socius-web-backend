package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageRequestDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.services.MessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketChatController.class);

    private final MessageService messageService;

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/conversations/{conversationId}")
    public Map<String, Object> handleChatMessage(
            @DestinationVariable String conversationId,
            Map<String, Object> message) {

        logger.info("Received WebSocket message for conversation {}: {}", conversationId, message);

        try {
            // Validate message structure
            if (!message.containsKey("data") || !(message.get("data") instanceof Map)) {
                throw new IllegalArgumentException("Invalid message format");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) message.get("data");

            // Create MessageRequestDto
            MessageRequestDto requestDto = MessageRequestDto.builder()
                    .conversationId(UUID.fromString(conversationId))
                    .content((String) data.get("content"))
                    .messageType(org.socius.sociuswebbackend.model.enums.MessageType.TEXT)
                    .build();

            // Send message through service (this will also handle RabbitMQ)
            MessageResponseDto responseDto = messageService.sendMessage(requestDto);

            logger.info("Message sent successfully via WebSocket: {}", responseDto.getId());

            // Return response to be sent to topic subscribers
            return Map.of(
                    "type", "CHAT_MESSAGE_SENT",
                    "messageId", responseDto.getId(),
                    "conversationId", conversationId,
                    "timestamp", responseDto.getCreatedAt(),
                    "success", true
            );

        } catch (Exception e) {
            logger.error("Error handling WebSocket chat message: {}", e.getMessage(), e);
            return Map.of(
                    "type", "CHAT_MESSAGE_ERROR",
                    "conversationId", conversationId,
                    "error", e.getMessage(),
                    "success", false
            );
        }
    }

    @MessageMapping("/typing/{conversationId}")
    @SendTo("/topic/conversations/{conversationId}/typing")
    public Map<String, Object> handleTyping(
            @DestinationVariable String conversationId,
            Map<String, Object> typingData,
            Principal principal) {

        logger.info("User {} typing in conversation {}", principal.getName(), conversationId);

        return Map.of(
                "type", "TYPING_INDICATOR",
                "conversationId", conversationId,
                "userId", principal.getName(),
                "isTyping", typingData.get("isTyping"),
                "timestamp", System.currentTimeMillis()
        );
    }
}