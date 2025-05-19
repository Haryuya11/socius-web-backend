package org.socius.sociuswebbackend.model.dtos.conversation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.MemberRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationMemberDto {
    private UUID conversationId;
    private UserResponseDto user;
    private LocalDateTime joinedAt;
    private MemberRole role;
}
