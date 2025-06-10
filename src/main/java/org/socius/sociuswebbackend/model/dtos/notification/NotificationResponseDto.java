package org.socius.sociuswebbackend.model.dtos.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.enums.NotificationType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponseDto extends BaseDto {
    private String title;
    private UserResponseDto sender;
    private String message;
    private LocalDate expiryDate;
    private NotificationType type;
    private Boolean isUrgent;
    
    @Builder.Default
    private List<NotificationRecipientDto> recipients = new ArrayList<>();
}
