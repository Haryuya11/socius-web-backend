package org.socius.sociuswebbackend.model.dtos.role;

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
@Builder
public class RolePermissionRequestDto {
    @NotNull(message = "Role ID must not be null")
    private UUID roleId;
    
    @NotNull(message = "Permission ID must not be null")
    private UUID permissionId;
}
