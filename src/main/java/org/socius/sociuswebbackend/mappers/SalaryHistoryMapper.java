package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.salary.SalaryHistoryRequestDto;
import org.socius.sociuswebbackend.model.dtos.salary.SalaryHistoryResponseDto;
import org.socius.sociuswebbackend.model.entities.SalaryHistoryEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for SalaryHistory entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class SalaryHistoryMapper extends BaseEntityMapper implements
        GenericMapper<SalaryHistoryEntity, SalaryHistoryResponseDto, SalaryHistoryRequestDto> {
    
    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract SalaryHistoryResponseDto entityToDto(SalaryHistoryEntity entity);
    
    @Override
    public SalaryHistoryEntity requestDtoToEntity(SalaryHistoryRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        
        return SalaryHistoryEntity.builder()
            .user(mappingUtil.mapUserIdToEntity(dto.getUserId()))
            .previousSalary(dto.getPreviousSalary())
            .newSalary(dto.getNewSalary())
            .effectiveDate(dto.getEffectiveDate())
            .reason(dto.getReason())
            .build();
    }
    
    @Override
    public void updateEntityFromDto(SalaryHistoryRequestDto dto, @MappingTarget SalaryHistoryEntity entity) {
        if (dto == null) {
            return;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        
        if (dto.getUserId() != null) {
            entity.setUser(mappingUtil.mapUserIdToEntity(dto.getUserId()));
        }
        
        if (dto.getPreviousSalary() != null) {
            entity.setPreviousSalary(dto.getPreviousSalary());
        }
        
        if (dto.getNewSalary() != null) {
            entity.setNewSalary(dto.getNewSalary());
        }
        
        if (dto.getEffectiveDate() != null) {
            entity.setEffectiveDate(dto.getEffectiveDate());
        }
        
        if (dto.getReason() != null) {
            entity.setReason(dto.getReason());
        }
    }
}
