package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.socius.sociuswebbackend.model.dtos.role.RolePermissionRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RolePermissionResponseDto;
import org.socius.sociuswebbackend.model.entities.PermissionEntity;
import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionId;

/**
 * Mapper for RolePermission entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class, PermissionMapper.class})
public abstract class RolePermissionMapper {

//    @Override
    public abstract RolePermissionResponseDto entityToDto(RolePermissionEntity entity);

    @Mapping(target = "id", expression = "java(createRolePermissionId(dto.getRoleId(), dto.getPermissionId()))")
    @Mapping(target = "role", expression = "java(createRoleReference(dto.getRoleId()))")
    @Mapping(target = "permission", expression = "java(createPermissionReference(dto.getPermissionId()))")
    public abstract RolePermissionEntity requestDtoToEntity(RolePermissionRequestDto dto);

    protected RolePermissionId createRolePermissionId(java.util.UUID roleId, java.util.UUID permissionId) {
        return RolePermissionId.builder()
                .roleId(roleId)
                .permissionId(permissionId)
                .build();
    }

    protected RoleEntity createRoleReference(java.util.UUID roleId) {
        return RoleEntity.builder().id(roleId).build();
    }

    protected PermissionEntity createPermissionReference(java.util.UUID permissionId) {
        return PermissionEntity.builder().id(permissionId).build();
    }
}
