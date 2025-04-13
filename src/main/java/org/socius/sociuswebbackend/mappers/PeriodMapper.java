package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.dtos.period.PeriodRequestDto;
import org.socius.sociuswebbackend.model.dtos.period.PeriodResponseDto;
import org.socius.sociuswebbackend.model.entities.PeriodEntity;

/**
 * Mapper for Period entities and DTOs
 */
@Mapper(componentModel = "spring")
public interface PeriodMapper extends BaseEntityMapper, 
        GenericMapper<PeriodEntity, PeriodResponseDto, PeriodRequestDto> {
    
    @Override
    PeriodResponseDto entityToDto(PeriodEntity entity);
    
    @Override
    PeriodEntity requestDtoToEntity(PeriodRequestDto dto);
    
    @Override
    void updateEntityFromDto(PeriodRequestDto dto, @MappingTarget PeriodEntity entity);
}
