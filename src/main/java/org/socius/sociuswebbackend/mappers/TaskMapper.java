package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for Task entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class TaskMapper extends BaseEntityMapper implements
        GenericMapper<TaskEntity, TaskResponseDto, TaskRequestDto> {

    @Override
    @Mapping(target = "assignedTo", source = "assignedTo", qualifiedByName = "toLimitedDto")
    public abstract TaskResponseDto entityToDto(TaskEntity entity);

    @Override
    public TaskEntity requestDtoToEntity(TaskRequestDto dto) {
        if (dto == null) {
            return null;
        }

        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        UserEntity assignedTo = dto.getAssignedToId() != null ?
                mappingUtil.mapUserIdToEntity(dto.getAssignedToId()) : null;

        return TaskEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .deadline(dto.getDeadline())
                .status(dto.getStatus())
                .assignedTo(assignedTo)
                .build();
    }

    @Override
    public void updateEntityFromDto(TaskRequestDto dto, @MappingTarget TaskEntity entity) {
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
