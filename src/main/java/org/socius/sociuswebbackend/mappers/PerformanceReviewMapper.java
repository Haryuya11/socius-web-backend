package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.performance.PerformanceReviewRequestDto;
import org.socius.sociuswebbackend.model.dtos.performance.PerformanceReviewResponseDto;
import org.socius.sociuswebbackend.model.entities.PerformanceReviewEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for PerformanceReview entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, PeriodMapper.class})
public abstract class PerformanceReviewMapper extends BaseEntityMapper implements
        GenericMapper<PerformanceReviewEntity, PerformanceReviewResponseDto, PerformanceReviewRequestDto> {

    @Override
    public abstract PerformanceReviewResponseDto entityToDto(PerformanceReviewEntity entity);

    @Override
    public PerformanceReviewEntity requestDtoToEntity(PerformanceReviewRequestDto dto) {
        if (dto == null) {
            return null;
        }

        EntityMappingUtil mappingUtil = getEntityMappingUtil();

        return PerformanceReviewEntity.builder()
                .employee(mappingUtil.mapUserIdToEntity(dto.getEmployeeId()))
                .reviewer(mappingUtil.mapUserIdToEntity(dto.getReviewerId()))
                .period(mappingUtil.mapPeriodIdToEntity(dto.getPeriodId()))
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();
    }

    @Override
    public void updateEntityFromDto(PerformanceReviewRequestDto dto, @MappingTarget PerformanceReviewEntity entity) {
        if (dto == null) {
            return;
        }

        EntityMappingUtil mappingUtil = getEntityMappingUtil();

        if (dto.getEmployeeId() != null) {
            entity.setEmployee(mappingUtil.mapUserIdToEntity(dto.getEmployeeId()));
        }

        if (dto.getReviewerId() != null) {
            entity.setReviewer(mappingUtil.mapUserIdToEntity(dto.getReviewerId()));
        }

        if (dto.getPeriodId() != null) {
            entity.setPeriod(mappingUtil.mapPeriodIdToEntity(dto.getPeriodId()));
        }

        if (dto.getRating() != null) {
            entity.setRating(dto.getRating());
        }

        if (dto.getComment() != null) {
            entity.setComment(dto.getComment());
        }
    }
}
