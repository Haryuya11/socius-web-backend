package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.login.LoginHistoryResponseDto;
import org.socius.sociuswebbackend.model.entities.LoginHistoryEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.time.LocalDateTime;

/**
 * Mapper for LoginHistory entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface LoginHistoryMapper extends BaseEntityMapper, 
        GenericMapper<LoginHistoryEntity, LoginHistoryResponseDto, LoginHistoryRequestDto> {
    
    @Override
    LoginHistoryResponseDto entityToDto(LoginHistoryEntity entity);
    
    @Override
    default LoginHistoryEntity requestDtoToEntity(LoginHistoryRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        UserEntity user = mappingUtil.mapUserIdToEntity(dto.getUserId());
        
        return LoginHistoryEntity.builder()
            .user(user)
            .loginTime(dto.getLoginTime() != null ? dto.getLoginTime() : LocalDateTime.now())
            .ipAddress(dto.getIpAddress())
            .deviceInfo(dto.getDeviceInfo())
            .build();
    }
    
    @Override
    default void updateEntityFromDto(LoginHistoryRequestDto dto, @MappingTarget LoginHistoryEntity entity) {
        if (dto == null) {
            return;
        }
        
        if (dto.getUserId() != null) {
            EntityMappingUtil mappingUtil = getEntityMappingUtil();
            entity.setUser(mappingUtil.mapUserIdToEntity(dto.getUserId()));
        }
        
        if (dto.getLoginTime() != null) {
            entity.setLoginTime(dto.getLoginTime());
        }
        
        if (dto.getIpAddress() != null) {
            entity.setIpAddress(dto.getIpAddress());
        }
        
        if (dto.getDeviceInfo() != null) {
            entity.setDeviceInfo(dto.getDeviceInfo());
        }
    }
}
