package org.socius.sociuswebbackend.security;

import lombok.Builder;
import lombok.Data;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;

import java.util.UUID;

@Data
@Builder
public class PermissionContext {
    private UserPermissionsDto currentUser;
    private Object[] methodArgs;
    private String[] paramNames;
    private String requestMethod;
    private String requestPath;

    // Helper methods để kiểm tra scope
    public UUID getTargetUserId() {
        // Tìm userId từ method arguments
        for (int i = 0; i < paramNames.length; i++) {
            if ("userId".equals(paramNames[i]) || "employeeId".equals(paramNames[i])) {
                return (UUID) methodArgs[i];
            }
        }
        return null;
    }

    public boolean isOwnResource() {
        UUID targetUserId = getTargetUserId();
        return targetUserId != null && targetUserId.equals(currentUser.getUserId());
    }
}