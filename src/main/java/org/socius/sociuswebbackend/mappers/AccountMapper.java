package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.account.AccountRequestDto;
import org.socius.sociuswebbackend.model.dtos.account.AccountResponseDto;
import org.socius.sociuswebbackend.model.entities.AccountEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for Account entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class AccountMapper extends BaseEntityMapper implements
        GenericMapper<AccountEntity, AccountResponseDto, AccountRequestDto> {
    
    @Override
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "isDefaultPassword", target = "isDefaultPassword")
    public abstract AccountResponseDto entityToDto(AccountEntity entity);
    
    @Override
    public AccountEntity requestDtoToEntity(AccountRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        UserEntity user = mappingUtil.mapUserIdToEntity(dto.getUserId());
        
        return AccountEntity.builder()
                .user(user)
                .password(dto.getPassword())
                .isActive(dto.getIsActive())
                .build();
    }
    
    @Override
    public void updateEntityFromDto(AccountRequestDto dto, @MappingTarget AccountEntity entity) {
        if (dto == null) {
            return;
        }
        
        if (dto.getUserId() != null) {
            EntityMappingUtil mappingUtil = getEntityMappingUtil();
            entity.setUser(mappingUtil.mapUserIdToEntity(dto.getUserId()));
        }
        
        if (dto.getPassword() != null) {
            entity.setPassword(dto.getPassword());
        }
        
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }
}
