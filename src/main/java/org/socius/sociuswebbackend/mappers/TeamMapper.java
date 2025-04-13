package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamWithMembersDto;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

/**
 * Mapper for Team entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TeamMapper extends BaseEntityMapper, 
        GenericMapper<TeamEntity, TeamResponseDto, TeamRequestDto> {
    
    @Override
    TeamResponseDto entityToDto(TeamEntity entity);
    
    @Override
    default TeamEntity requestDtoToEntity(TeamRequestDto dto) {
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
    default void updateEntityFromDto(TeamRequestDto dto, @MappingTarget TeamEntity entity) {
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
     * Map TeamEntity to TeamWithMembersDto
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "leader", target = "leader")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "memberCount", ignore = true)
    TeamWithMembersDto entityToWithMembersDto(TeamEntity entity);
}
