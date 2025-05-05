package org.socius.sociuswebbackend.model.dtos.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.socius.sociuswebbackend.model.dtos.BaseDto;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class RoleResponseDto extends BaseDto {
    private String name;
    private String description;
    
    @Builder.Default
    private Set<PermissionResponseDto> permissions = new HashSet<>();
}