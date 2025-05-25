package org.socius.sociuswebbackend.model.dtos.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.enums.MessageType;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequestDto {
    @NotNull(message = "conversationId không được để trống")
    private UUID conversationId;

    private String content;

    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    private String fileUrl;
    private String fileOriginalName;
    private String fileContentType;
    private Long fileSize;
}
