package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.entities.DepartmentEntity;

/**
 * Mapper for Department entities and DTOs
 */
@Mapper(componentModel = "spring")
public interface DepartmentMapper extends BaseEntityMapper, 
        GenericMapper<DepartmentEntity, DepartmentResponseDto, DepartmentRequestDto> {
    
    @Override
    DepartmentResponseDto entityToDto(DepartmentEntity entity);
    
    @Override
    DepartmentEntity requestDtoToEntity(DepartmentRequestDto dto);
    
    @Override
    void updateEntityFromDto(DepartmentRequestDto dto, @MappingTarget DepartmentEntity entity);
}
