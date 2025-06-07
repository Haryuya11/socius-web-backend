package org.socius.sociuswebbackend.model.dtos.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionResponseDto {
    private RoleResponseDto role;
    private PermissionResponseDto permission;
}
