package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.target.TargetRequestDto;
import org.socius.sociuswebbackend.model.dtos.target.TargetResponseDto;
import org.socius.sociuswebbackend.model.entities.TargetEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for Target entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class TargetMapper extends BaseEntityMapper implements
        GenericMapper<TargetEntity, TargetResponseDto, TargetRequestDto> {

    @Override
    public abstract TargetResponseDto entityToDto(TargetEntity entity);

    @Override
    public TargetEntity requestDtoToEntity(TargetRequestDto dto) {
        if (dto == null) {
            return null;
        }

        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        UserEntity assignedTo = dto.getAssignedToId() != null ?
                mappingUtil.mapUserIdToEntity(dto.getAssignedToId()) : null;

        return TargetEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .deadline(dto.getDeadline())
                .status(dto.getStatus())
                .assignedTo(assignedTo)
                .build();
    }

    @Override
    public void updateEntityFromDto(TargetRequestDto dto, @MappingTarget TargetEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }

        if (dto.getDeadline() != null) {
            entity.setDeadline(dto.getDeadline());
        }

        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        if (dto.getAssignedToId() != null) {
            EntityMappingUtil mappingUtil = getEntityMappingUtil();
            entity.setAssignedTo(mappingUtil.mapUserIdToEntity(dto.getAssignedToId()));
        }
    }
}
