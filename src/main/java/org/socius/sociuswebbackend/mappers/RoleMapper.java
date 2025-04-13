package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.entities.PermissionEntity;
import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionId;
import org.socius.sociuswebbackend.util.ApplicationContextHelper;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for Role entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public interface RoleMapper extends BaseEntityMapper, 
        GenericMapper<RoleEntity, RoleResponseDto, RoleRequestDto> {
    
    @Override
    @Mapping(target = "permissions", ignore = true)
    RoleResponseDto entityToDto(RoleEntity entity);
    
    /**
     * Process permissions after entity mapping
     */
    @AfterMapping
    default void mapPermissions(@MappingTarget RoleResponseDto dto, RoleEntity entity) {
        if (entity.getRolePermissions() == null || entity.getRolePermissions().isEmpty()) {
            dto.setPermissions(new HashSet<>());
            return;
        }
        
        PermissionMapper permissionMapper = ApplicationContextHelper.getBean(PermissionMapper.class);
        Set<PermissionResponseDto> permissions = entity.getRolePermissions().stream()
            .filter(rp -> rp != null && rp.getPermission() != null)
            .map(rp -> permissionMapper.entityToDto(rp.getPermission()))
            .collect(Collectors.toSet());
            
        dto.setPermissions(permissions);
    }
    
    @Override
    @Mapping(target = "rolePermissions", ignore = true)
    RoleEntity requestDtoToEntity(RoleRequestDto dto);
    
    @Override
    @Mapping(target = "rolePermissions", ignore = true)
    void updateEntityFromDto(RoleRequestDto dto, @MappingTarget RoleEntity entity);
    
    /**
     * Post-processing after entity update to handle permissions
     */
    @AfterMapping
    default void updateRolePermissions(RoleRequestDto dto, @MappingTarget RoleEntity entity) {
        if (dto.getPermissionIds() != null) {
            if (entity.getRolePermissions() == null) {
                entity.setRolePermissions(new HashSet<>());
            } else {
                entity.getRolePermissions().clear();
            }
            
            EntityMappingUtil mappingUtil = getEntityMappingUtil();
            
            dto.getPermissionIds().stream()
                .filter(permissionId -> permissionId != null)
                .forEach(permissionId -> {
                    PermissionEntity permission = mappingUtil.mapPermissionIdToEntity(permissionId);
                    
                    RolePermissionId id = new RolePermissionId(entity.getId(), permissionId);
                    RolePermissionEntity rolePermission = RolePermissionEntity.builder()
                        .id(id)
                        .role(entity)
                        .permission(permission)
                        .build();
                        
                    entity.getRolePermissions().add(rolePermission);
                });
        }
    }
}
