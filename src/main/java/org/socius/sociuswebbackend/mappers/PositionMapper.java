package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.entities.PositionEntity;

/**
 * Mapper for Position entities and DTOs
 */
@Mapper(componentModel = "spring")
public abstract class PositionMapper extends BaseEntityMapper implements
        GenericMapper<PositionEntity, PositionResponseDto, PositionRequestDto> {

    @Override
    public abstract PositionResponseDto entityToDto(PositionEntity entity);

    @Override
    public abstract PositionEntity requestDtoToEntity(PositionRequestDto dto);

    @Override
    public abstract void updateEntityFromDto(PositionRequestDto dto, @MappingTarget PositionEntity entity);
}
