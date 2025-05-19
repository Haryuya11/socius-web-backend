package org.socius.sociuswebbackend.model.dtos.message;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReadReceiptDto {
    @NotNull(message = "conversationId không được để trống")
    private UUID conversationId;

    @NotNull(message = "lastReadMessageId không được để trống")
    private UUID lastReadMessageId;
}
