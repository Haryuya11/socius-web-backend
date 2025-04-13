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
public interface PositionMapper extends BaseEntityMapper, 
        GenericMapper<PositionEntity, PositionResponseDto, PositionRequestDto> {

    @Override
    PositionResponseDto entityToDto(PositionEntity entity);
    
    @Override
    PositionEntity requestDtoToEntity(PositionRequestDto dto);
    
    @Override
    void updateEntityFromDto(PositionRequestDto dto, @MappingTarget PositionEntity entity);
}
