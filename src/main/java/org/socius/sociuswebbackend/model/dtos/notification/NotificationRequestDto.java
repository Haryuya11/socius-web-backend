package org.socius.sociuswebbackend.model.dtos.notification;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.enums.NotificationType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDto {
    @NotBlank(message = "Title must not be empty")
    private String title;
    
    @NotNull(message = "Sender ID must not be null")
    private UUID senderId;
    
    @NotBlank(message = "Message must not be empty")
    private String message;
    
    @NotNull(message = "Expiry date must not be null")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;
    
    @NotNull(message = "Notification type must not be null")
    private NotificationType type;
    
    @NotNull(message = "Is urgent flag must not be null")
    private Boolean isUrgent;
    
    @Builder.Default
    private List<UUID> recipientIds = new ArrayList<>();
}
