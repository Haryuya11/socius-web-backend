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
public abstract class PeriodMapper extends BaseEntityMapper implements
        GenericMapper<PeriodEntity, PeriodResponseDto, PeriodRequestDto> {

    @Override
    public abstract PeriodResponseDto entityToDto(PeriodEntity entity);

    @Override
    public abstract PeriodEntity requestDtoToEntity(PeriodRequestDto dto);

    @Override
    public abstract void updateEntityFromDto(PeriodRequestDto dto, @MappingTarget PeriodEntity entity);
}
