package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionRequestDto;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;
import org.socius.sociuswebbackend.model.entities.PermissionEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for Permission entities and DTOs
 */
@Mapper(componentModel = "spring")
public abstract class PermissionMapper extends BaseEntityMapper implements
        GenericMapper<PermissionEntity, PermissionResponseDto, PermissionRequestDto> {

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract PermissionResponseDto entityToDto(PermissionEntity entity);

    @Named("entityToLimitedDto")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract PermissionResponseDto entityToLimitedDto(PermissionEntity entity);

    @Override
    public abstract PermissionEntity requestDtoToEntity(PermissionRequestDto dto);

    @Override
    public abstract void updateEntityFromDto(PermissionRequestDto dto, @MappingTarget PermissionEntity entity);

    /**
     * Convert set of RolePermissionEntity to set of PermissionResponseDto
     */
    @Named("rolePermissionsToPermissionDtos")
    public Set<PermissionResponseDto> rolePermissionsToPermissionDtos(Set<RolePermissionEntity> rolePermissions) {
        if (rolePermissions == null) {
            return new HashSet<>();
        }
        return rolePermissions.stream()
                .filter(rp -> rp != null && rp.getPermission() != null)
                .map(rp -> entityToDto(rp.getPermission()))
                .collect(Collectors.toSet());
    }

    @Named("rolePermissionsToLimitedPermissionDtos")
    public Set<PermissionResponseDto> rolePermissionsToLimitedPermissionDtos(Set<RolePermissionEntity> rolePermissions) {
        if (rolePermissions == null) {
            return new HashSet<>();
        }
        return rolePermissions.stream()
                .filter(rp -> rp != null && rp.getPermission() != null)
                .map(rp -> entityToLimitedDto(rp.getPermission()))
                .collect(Collectors.toSet());
    }
}
