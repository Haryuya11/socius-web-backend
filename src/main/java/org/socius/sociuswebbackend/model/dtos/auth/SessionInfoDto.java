package org.socius.sociuswebbackend.model.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionInfoDto {
    private UUID userId;
    private String username;
    private String fullName;
    private String email;
    private String imageUrl;
    private String sessionId;
    private LocalDateTime sessionCreationTime;
    private LocalDateTime sessionExpiryTime;
    private String ipAddress;
    private String deviceInfo;
    private RoleResponseDto role;
}
