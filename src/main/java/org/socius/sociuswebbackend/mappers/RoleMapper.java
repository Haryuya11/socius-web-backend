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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for Role entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {PermissionMapper.class})
public abstract class RoleMapper extends BaseEntityMapper implements
        GenericMapper<RoleEntity, RoleResponseDto, RoleRequestDto> {

    @Override
    @Mapping(target = "permissions", ignore = true)
    public RoleResponseDto entityToDto(RoleEntity entity) {
        if (entity == null) {
            return null;
        }

        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        if (entity.getRolePermissions() != null) {
            PermissionMapper permissionMapper = ApplicationContextHelper.getBean(PermissionMapper.class);
            dto.setPermissions(permissionMapper.rolePermissionsToPermissionDtos(entity.getRolePermissions()));
        }

        return dto;
    }

    @Named("entityToLimitedDto")
    @Mapping(target = "permissions", ignore = true)
    public RoleResponseDto entityToLimitedDto(RoleEntity entity) {
        if (entity == null) {
            return null;
        }

        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        if (entity.getRolePermissions() != null) {
            PermissionMapper permissionMapper = ApplicationContextHelper.getBean(PermissionMapper.class);
            dto.setPermissions(permissionMapper.rolePermissionsToLimitedPermissionDtos(entity.getRolePermissions()));
        }

        return dto;
    }

    /**
     * Process permissions after entity mapping
     */
//    @AfterMapping
//    default void mapPermissions(@MappingTarget RoleResponseDto dto, RoleEntity entity) {
//        if (entity.getRolePermissions() == null || entity.getRolePermissions().isEmpty()) {
//            dto.setPermissions(new HashSet<>());
//            return;
//        }
//
//        PermissionMapper permissionMapper = ApplicationContextHelper.getBean(PermissionMapper.class);
//        Set<PermissionResponseDto> permissions = entity.getRolePermissions().stream()
//                .filter(rp -> rp != null && rp.getPermission() != null)
//                .map(rp -> permissionMapper.entityToDto(rp.getPermission()))
//                .collect(Collectors.toSet());
//
//        dto.setPermissions(permissions);
//    }

    /*@AfterMapping
    public void mapPermissions(@MappingTarget RoleResponseDto dto, RoleEntity entity) {
        if (entity != null && entity.getRolePermissions() != null) {
            PermissionMapper permissionMapper = ApplicationContextHelper.getBean(PermissionMapper.class);
            dto.setPermissions(permissionMapper.rolePermissionsToPermissionDtos(entity.getRolePermissions()));
        }
    }*/

    @Override
    @Mapping(target = "rolePermissions", ignore = true)
    public RoleEntity requestDtoToEntity(RoleRequestDto dto){
        if (dto == null) {
            return null;
        }

        return RoleEntity.builder()
            .name(dto.getName())
            .description(dto.getDescription())
            .build();
    }

    @Override
    @Mapping(target = "rolePermissions", ignore = true)
    public void updateEntityFromDto(RoleRequestDto dto, @MappingTarget RoleEntity entity){
        if( dto == null) {
            return;
        }
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
    }

    /**
     * Post-processing after entity update to handle permissions
     */
    @AfterMapping
    public void updateRolePermissions(RoleRequestDto dto, @MappingTarget RoleEntity entity) {
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

    /**
     * Create RolePermission entities from permission IDs
     */
    public Set<RolePermissionEntity> createRolePermissions(RoleEntity role, Set<UUID> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>();
        }

        EntityMappingUtil mappingUtil = ApplicationContextHelper.getBean(EntityMappingUtil.class);
        return permissionIds.stream()
                .map(id -> {
                    PermissionEntity permission = mappingUtil.mapPermissionIdToEntity(id);
                    if (permission == null) {
                        return null;
                    }

                    RolePermissionEntity rolePermission = new RolePermissionEntity();
                    rolePermission.setId(new RolePermissionId(role.getId(), permission.getId()));
                    rolePermission.setRole(role);
                    rolePermission.setPermission(permission);
                    return rolePermission;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
