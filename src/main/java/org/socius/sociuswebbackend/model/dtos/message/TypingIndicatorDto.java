package org.socius.sociuswebbackend.model.dtos.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypingIndicatorDto {
    private UUID conversationId;
    private UUID userId;
    private String userName;
    private boolean isTyping;
}