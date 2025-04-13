package org.socius.sociuswebbackend.model.dtos.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotificationRecipientRequestDto {
    @NotNull(message = "Notification ID must not be null")
    private UUID notificationId;
    
    @NotNull(message = "User ID must not be null")
    private UUID userId;
    
    @Builder.Default
    private Boolean isRead = false;
}
