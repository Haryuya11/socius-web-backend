package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for Team entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class TeamMapper extends BaseEntityMapper implements
        GenericMapper<TeamEntity, TeamResponseDto, TeamRequestDto> {

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract TeamResponseDto entityToDto(TeamEntity entity);

    @Named("entityToLimitedDto")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "leader", ignore = true)
    public abstract TeamResponseDto entityToLimitedDto(TeamEntity entity);

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    public TeamEntity requestDtoToEntity(TeamRequestDto dto) {
        if (dto == null) {
            return null;
        }

        EntityMappingUtil mappingUtil = getEntityMappingUtil();
        UserEntity leader = dto.getLeaderId() != null ?
                mappingUtil.mapUserIdToEntity(dto.getLeaderId()) : null;

        return TeamEntity.builder()
                .name(dto.getName())
                .leader(leader)
                .build();
    }

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    public void updateEntityFromDto(TeamRequestDto dto, @MappingTarget TeamEntity entity) {
        if (dto == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getLeaderId() != null) {
            EntityMappingUtil mappingUtil = getEntityMappingUtil();
            entity.setLeader(mappingUtil.mapUserIdToEntity(dto.getLeaderId()));
        }
    }

    /**
     * Get list of member IDs from TeamEntity
     */
    public List<UUID> getMemberIds(TeamEntity entity) {
        if (entity == null || entity.getEmploymentDetailEntities() == null) {
            return List.of();
        }

        return entity.getEmploymentDetailEntities().stream()
                .filter(detail -> detail.getUser() != null)
                .map(detail -> detail.getUser().getId())
                .collect(Collectors.toList());
    }
}
