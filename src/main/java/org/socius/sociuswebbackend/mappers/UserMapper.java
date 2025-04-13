package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.dtos.user.UserRequestDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;

/**
 * Mapper for User entities and DTOs
 */
@Mapper(componentModel = "spring")
public interface UserMapper extends BaseEntityMapper, 
        GenericMapper<UserEntity, UserResponseDto, UserRequestDto> {
    
    @Override
    UserResponseDto entityToDto(UserEntity entity);
    
    @Override
    UserEntity requestDtoToEntity(UserRequestDto dto);
    
    @Override
    void updateEntityFromDto(UserRequestDto dto, @MappingTarget UserEntity entity);
}
