package org.socius.sociuswebbackend.model.dtos.message;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncMessagesRequestDto {
    @NotNull(message = "Danh sách điểm đồng bộ không được để trống")
    @Builder.Default
    private Map<UUID, UUID> lastMessageIds = new HashMap<>(); // Map của conversationId -> lastMessageId
}