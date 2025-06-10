package org.socius.sociuswebbackend.model.dtos.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleResponseDto extends BaseDto {
    private String name;
    private String description;

    @Builder.Default
    private Set<PermissionResponseDto> permissions = new HashSet<>();
}