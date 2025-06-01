package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
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
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
    public abstract UserResponseDto entityToDto(UserEntity entity);

    @Named("toLimitedDto")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "nationality", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "hireDate", ignore = true)
    @Mapping(target = "address", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
    public abstract UserResponseDto toLimitedDto(UserEntity entity);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract UserEntity requestDtoToEntity(UserRequestDto dto);

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract void updateEntityFromDto(UserRequestDto dto, @MappingTarget UserEntity entity);
}