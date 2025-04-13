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
public interface RolePermissionMapper {
    
    @Mapping(source = "role", target = "role")
    @Mapping(source = "permission", target = "permission")
    RolePermissionResponseDto entityToDto(RolePermissionEntity entity);
    
    default RolePermissionEntity requestDtoToEntity(RolePermissionRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        RolePermissionId id = new RolePermissionId(dto.getRoleId(), dto.getPermissionId());
        
        RoleEntity role = new RoleEntity();
        role.setId(dto.getRoleId());
        
        PermissionEntity permission = new PermissionEntity();
        permission.setId(dto.getPermissionId());
        
        return RolePermissionEntity.builder()
            .id(id)
            .role(role)
            .permission(permission)
            .build();
    }
}
