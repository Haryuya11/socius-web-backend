package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.ranking.EmployeeRankingRequestDto;
import org.socius.sociuswebbackend.model.dtos.ranking.EmployeeRankingResponseDto;
import org.socius.sociuswebbackend.model.entities.EmployeeRankingEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for EmployeeRanking entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, PeriodMapper.class})
public interface EmployeeRankingMapper extends BaseEntityMapper, 
        GenericMapper<EmployeeRankingEntity, EmployeeRankingResponseDto, EmployeeRankingRequestDto> {
    
    @Override
    EmployeeRankingResponseDto entityToDto(EmployeeRankingEntity entity);
    
    @Override
    default EmployeeRankingEntity requestDtoToEntity(EmployeeRankingRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        
        return EmployeeRankingEntity.builder()
                .employee(mappingUtil.mapUserIdToEntity(dto.getEmployeeId()))
                .period(mappingUtil.mapPeriodIdToEntity(dto.getPeriodId()))
                .rank(dto.getRank())
                .criteria(dto.getCriteria())
                .build();
    }
    
    @Override
    default void updateEntityFromDto(EmployeeRankingRequestDto dto, @MappingTarget EmployeeRankingEntity entity) {
        if (dto == null) {
            return;
        }
        
        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        
        if (dto.getEmployeeId() != null) {
            entity.setEmployee(mappingUtil.mapUserIdToEntity(dto.getEmployeeId()));
        }
        
        if (dto.getPeriodId() != null) {
            entity.setPeriod(mappingUtil.mapPeriodIdToEntity(dto.getPeriodId()));
        }
        
        if (dto.getRank() != null) {
            entity.setRank(dto.getRank());
        }
        
        if (dto.getCriteria() != null) {
            entity.setCriteria(dto.getCriteria());
        }
    }
}
