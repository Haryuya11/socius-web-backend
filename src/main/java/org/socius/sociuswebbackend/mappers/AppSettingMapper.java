package org.socius.sociuswebbackend.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.dtos.config.AppSettingUpdateRequestDto;
import org.socius.sociuswebbackend.model.dtos.config.ConfigDto;
import org.socius.sociuswebbackend.model.entities.AppSettingsEntity;

@Mapper(componentModel = "spring")
public abstract class AppSettingMapper extends BaseEntityMapper implements
        GenericMapper<AppSettingsEntity, ConfigDto, AppSettingUpdateRequestDto> {

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract ConfigDto entityToDto(AppSettingsEntity entity);

    @Override
    public abstract AppSettingsEntity requestDtoToEntity(AppSettingUpdateRequestDto dto);

    @Override
    public abstract void updateEntityFromDto(AppSettingUpdateRequestDto dto, @MappingTarget AppSettingsEntity entity);
}
