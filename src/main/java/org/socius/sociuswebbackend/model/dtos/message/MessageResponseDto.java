package org.socius.sociuswebbackend.model.dtos.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.MessageType;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MessageResponseDto extends BaseDto {
    private UUID conversationId;
    private UserResponseDto sender;
    private String content;
    private MessageType messageType;
    private boolean isEdited;
    private boolean isDeleted;
    private boolean isRead;

    private String fileUrl;
    private String fileOriginalName;
    private String fileContentType;
    private Long fileSize;
}