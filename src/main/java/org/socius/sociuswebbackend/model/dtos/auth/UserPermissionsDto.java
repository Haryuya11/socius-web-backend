package org.socius.sociuswebbackend.model.dtos.auth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsDto {
    private UUID userId;
    private UUID roleId;
    private String roleName;
    private Set<String> permissions;
}
