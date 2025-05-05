package org.socius.sociuswebbackend.model.dtos.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRecipientDto {
    private UUID notificationId;
    private UUID userId;
    private UserResponseDto user;
    private Boolean isRead;
    private LocalDateTime readAt;
}
