package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.entities.DepartmentEntity;

/**
 * Mapper for Department entities and DTOs
 */
@Mapper(componentModel = "spring")
public abstract class DepartmentMapper extends BaseEntityMapper implements
        GenericMapper<DepartmentEntity, DepartmentResponseDto, DepartmentRequestDto> {

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract DepartmentResponseDto entityToDto(DepartmentEntity entity);

    @Named("entityToLimitedDto")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract DepartmentResponseDto entityToLimitedDto(DepartmentEntity entity);

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract DepartmentEntity requestDtoToEntity(DepartmentRequestDto dto);

    @Override
    public abstract void updateEntityFromDto(DepartmentRequestDto dto, @MappingTarget DepartmentEntity entity);
}
