package org.socius.sociuswebbackend.model.dtos.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestDto {
    private String name;
    
    private String description;
    
    @Builder.Default
    private Set<UUID> permissionIds = new HashSet<>();
}
