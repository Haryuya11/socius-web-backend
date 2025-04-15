package org.socius.sociuswebbackend.model.dtos.auth;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import lombok.Data;

@Data
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
    private Set<String> permissions;
}
