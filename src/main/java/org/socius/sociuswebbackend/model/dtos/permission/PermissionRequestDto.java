package org.socius.sociuswebbackend.model.dtos.permission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionRequestDto {
    @NotBlank(message = "Permission name must not be empty")
    private String name;
    
    private String description;
}
