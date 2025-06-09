package org.socius.sociuswebbackend.model.dtos.message;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    private String displayUrl;

    // Getter tự động tạo displayUrl nếu có fileUrl
    public String getDisplayUrl() {
        if (fileUrl != null && !fileUrl.isEmpty()) {
            return "/api/static/" + fileUrl;
        }
        return displayUrl;
    }
}