package org.socius.sociuswebbackend.model.dtos.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequestDto {
    @NotBlank(message = "Role name must not be empty")
    private String name;
    
    private String description;
    
    @Builder.Default
    private Set<UUID> permissionIds = new HashSet<>();
}
