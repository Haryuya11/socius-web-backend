package org.socius.sociuswebbackend.model.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.enums.InvalidationReason;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInvalidationMessage {
    private UUID roleId;
    private Set<String> sessionIds;
    private InvalidationReason reason;
    private String message;
    private boolean forceAllUsersWithRole;
}
