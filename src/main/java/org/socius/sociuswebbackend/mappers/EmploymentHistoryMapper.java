package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentHistoryResponseDto;
import org.socius.sociuswebbackend.model.entities.EmploymentHistoryEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for EmploymentHistory entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {
        UserMapper.class, 
        PositionMapper.class, 
        DepartmentMapper.class, 
        TeamMapper.class, 
        RoleMapper.class
})
public interface EmploymentHistoryMapper extends BaseEntityMapper, 
        GenericMapper<EmploymentHistoryEntity, EmploymentHistoryResponseDto, EmploymentHistoryRequestDto> {
    
    @Override
    EmploymentHistoryResponseDto entityToDto(EmploymentHistoryEntity entity);
    
    @Override
    default EmploymentHistoryEntity requestDtoToEntity(EmploymentHistoryRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        
        return EmploymentHistoryEntity.builder()
                .user(mappingUtil.mapUserIdToEntity(dto.getUserId()))
                .position(mappingUtil.mapPositionIdToEntity(dto.getPositionId()))
                .department(mappingUtil.mapDepartmentIdToEntity(dto.getDepartmentId()))
                .team(dto.getTeamId() != null ? mappingUtil.mapTeamIdToEntity(dto.getTeamId()) : null)
                .role(mappingUtil.mapRoleIdToEntity(dto.getRoleId()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .salary(dto.getSalary())
                .description(dto.getDescription())
                .build();
    }
    
    @Override
    default void updateEntityFromDto(EmploymentHistoryRequestDto dto, @MappingTarget EmploymentHistoryEntity entity) {
        if (dto == null) {
            return;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        
        if (dto.getUserId() != null) {
            entity.setUser(mappingUtil.mapUserIdToEntity(dto.getUserId()));
        }
        
        if (dto.getPositionId() != null) {
            entity.setPosition(mappingUtil.mapPositionIdToEntity(dto.getPositionId()));
        }
        
        if (dto.getDepartmentId() != null) {
            entity.setDepartment(mappingUtil.mapDepartmentIdToEntity(dto.getDepartmentId()));
        }
        
        if (dto.getTeamId() != null) {
            entity.setTeam(mappingUtil.mapTeamIdToEntity(dto.getTeamId()));
        }
        
        if (dto.getRoleId() != null) {
            entity.setRole(mappingUtil.mapRoleIdToEntity(dto.getRoleId()));
        }
        
        if (dto.getStartDate() != null) {
            entity.setStartDate(dto.getStartDate());
        }
        
        if (dto.getEndDate() != null) {
            entity.setEndDate(dto.getEndDate());
        }
        
        if (dto.getSalary() != null) {
            entity.setSalary(dto.getSalary());
        }
        
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
    }
}
