package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailRequestDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for EmploymentDetail entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {
        UserMapper.class,
        PositionMapper.class,
        DepartmentMapper.class,
        TeamMapper.class,
        RoleMapper.class
})
public abstract class EmploymentDetailMapper extends BaseEntityMapper implements
        GenericMapper<EmploymentDetailEntity, EmploymentDetailResponseDto, EmploymentDetailRequestDto> {

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract EmploymentDetailResponseDto entityToDto(EmploymentDetailEntity entity);

    @Named("entityToLimitedDto")
    @Mapping(target = "user", source = "user", qualifiedByName = "toLimitedDto")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "role", ignore = true) // Bỏ trường role
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "salary", ignore = true)
    @Mapping(target = "workingStatus", source = "workingStatus")
    public abstract EmploymentDetailResponseDto entityToLimitedDto(EmploymentDetailEntity entity);

    @Named("entityToLimitedDto")
    @Mapping(target = "user", source = "user", qualifiedByName = "toLimitedDto")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "position", source = "position")
    @Mapping(target = "department", source = "department")
    @Mapping(target = "team", source = "team")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "salary", ignore = true)
    @Mapping(target = "workingStatus", source = "workingStatus")
    public abstract EmploymentDetailResponseDto entityToLimitedDtoForAdmin(EmploymentDetailEntity entity);

    @Override
    public EmploymentDetailEntity requestDtoToEntity(EmploymentDetailRequestDto dto) {
        if (dto == null) {
            return null;
        }

        EntityMappingUtil mappingUtil = getEntityMappingUtil();

        return EmploymentDetailEntity.builder()
                .user(mappingUtil.mapUserIdToEntity(dto.getUserId()))
                .position(mappingUtil.mapPositionIdToEntity(dto.getPositionId()))
                .department(mappingUtil.mapDepartmentIdToEntity(dto.getDepartmentId()))
                .team(dto.getTeamId() != null ? mappingUtil.mapTeamIdToEntity(dto.getTeamId()) : null)
                .role(mappingUtil.mapRoleIdToEntity(dto.getRoleId()))
                .startDate(dto.getStartDate())
                .salary(dto.getSalary())
                .workingStatus(dto.getWorkingStatus())
                .build();
    }

    @Override
    public void updateEntityFromDto(EmploymentDetailRequestDto dto, @MappingTarget EmploymentDetailEntity entity) {
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

        if (dto.getSalary() != null) {
            entity.setSalary(dto.getSalary());
        }

        if (dto.getWorkingStatus() != null) {
            entity.setWorkingStatus(dto.getWorkingStatus());
        }
    }
}
