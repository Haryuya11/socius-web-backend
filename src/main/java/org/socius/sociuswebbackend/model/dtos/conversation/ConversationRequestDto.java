package org.socius.sociuswebbackend.model.dtos.conversation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.enums.ConversationType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationRequestDto {

    private String name;

    @NotNull(message = "Loại cuộc trò chuyện không được để trống")
    private ConversationType type;

    @NotEmpty(message = "Danh sách thành viên không được để trống")
    @Builder.Default
    private Set<UUID> memberIds = new HashSet<>();
}
