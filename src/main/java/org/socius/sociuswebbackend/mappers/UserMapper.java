package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.dtos.user.UserRequestDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;

/**
 * Mapper for User entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public abstract class UserMapper extends BaseEntityMapper implements
        GenericMapper<UserEntity, UserResponseDto, UserRequestDto> {

    @Override
    public abstract UserResponseDto entityToDto(UserEntity entity);

    @Override
    public abstract UserEntity requestDtoToEntity(UserRequestDto dto);

    @Override
    public abstract void updateEntityFromDto(UserRequestDto dto, @MappingTarget UserEntity entity);
}