package org.socius.sociuswebbackend.model.dtos.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RolePermissionResponseDto {
    private RoleResponseDto role;
    private PermissionResponseDto permission;
}
