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
public interface UserMapper extends BaseEntityMapper,
        GenericMapper<UserEntity, UserResponseDto, UserRequestDto> {

    @Override
//    @Mapping(target = "fullName", expression = "java(entity.getFirstName() + \" \" + entity.getLastName())")
    @Mapping(source = "employmentDetail.role", target = "role")
    UserResponseDto entityToDto(UserEntity entity);

    @Override
    UserEntity requestDtoToEntity(UserRequestDto dto);

    @Override
    void updateEntityFromDto(UserRequestDto dto, @MappingTarget UserEntity entity);
}
