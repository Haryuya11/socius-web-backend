package org.socius.sociuswebbackend.model.dtos.conversation;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.ConversationType;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ConversationResponseDto extends BaseDto {
    private String name;
    private ConversationType type;
    private UserResponseDto createdBy;
    private MessageResponseDto lastMessage;
    private int unreadCount;
    private String imageUrl;


    @Builder.Default
    private Set<ConversationMemberDto> members = new HashSet<>();
}
