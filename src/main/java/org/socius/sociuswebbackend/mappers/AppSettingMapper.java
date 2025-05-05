package org.socius.sociuswebbackend.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.socius.sociuswebbackend.model.dtos.config.AppSettingUpdateRequestDto;
import org.socius.sociuswebbackend.model.dtos.config.ConfigDto;
import org.socius.sociuswebbackend.model.entities.AppSettingsEntity;

@Mapper(componentModel = "spring")
public interface AppSettingMapper extends BaseEntityMapper,
        GenericMapper<AppSettingsEntity, ConfigDto, AppSettingUpdateRequestDto> {

    @Override
    ConfigDto entityToDto(AppSettingsEntity entity);

    @Override
    AppSettingsEntity requestDtoToEntity(AppSettingUpdateRequestDto dto);

    @Override
    void updateEntityFromDto(AppSettingUpdateRequestDto dto, @MappingTarget AppSettingsEntity entity);
}
