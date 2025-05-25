package org.socius.sociuswebbackend.model.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserStatusDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID userId;
    private String fullName;
    private String imageUrl;
    private String sessionId;
    private LocalDateTime lastSeen;
}
