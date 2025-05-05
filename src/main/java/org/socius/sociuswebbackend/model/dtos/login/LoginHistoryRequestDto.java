package org.socius.sociuswebbackend.model.dtos.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistoryRequestDto {
    @NotNull(message = "User ID must not be null")
    private UUID userId;
    
    private LocalDateTime loginTime;
    
    private String ipAddress;
    
    private String deviceInfo;
}
