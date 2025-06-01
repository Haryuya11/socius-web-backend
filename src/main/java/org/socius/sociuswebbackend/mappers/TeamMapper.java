package org.socius.sociuswebbackend.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamWithMembersDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for Team entities and DTOs
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class TeamMapper extends BaseEntityMapper implements
        GenericMapper<TeamEntity, TeamResponseDto, TeamRequestDto> {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
    public abstract TeamResponseDto entityToDto(TeamEntity entity);

    @Named("entityToLimitedDto")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "leader", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "createdBy", ignore = true)
//    @Mapping(target = "updatedBy", ignore = true)
    public abstract TeamResponseDto entityToLimitedDto(TeamEntity entity);

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
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
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
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
     * Map TeamEntity to TeamWithMembersDto
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "leader", target = "leader")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "memberCount", ignore = true)
    public abstract TeamWithMembersDto entityToWithMembersDto(TeamEntity entity);

    public Map<String, Object> entityToTeamWithMembers(TeamEntity entity, org.springframework.data.domain.Pageable pageable) {
        if (entity == null) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        result.put("name", entity.getName());
        result.put("leader", entity.getLeader() != null ? userMapper.toLimitedDto(entity.getLeader()) : null);

        List<Map<String, Object>> members = entity.getEmploymentDetailEntities().stream()
                .filter(detail -> detail != null && detail.getUser() != null)
                .map(detail -> {
                    Map<String, Object> member = new HashMap<>();
                    member.put("user", userMapper.toLimitedDto(detail.getUser()));

                    // Tạo Map cho employmentDetail thủ công
                    Map<String, Object> employmentDetail = new HashMap<>();
                    if (detail.getPosition() != null) {
                        employmentDetail.put("position", Map.of(
                                "id", detail.getPosition().getId(),
                                "name", detail.getPosition().getName()
                        ));
                    }
                    if (detail.getDepartment() != null) {
                        employmentDetail.put("department", Map.of(
                                "id", detail.getDepartment().getId(),
                                "name", detail.getDepartment().getName()
                        ));
                    }
                    if (detail.getTeam() != null) {
                        employmentDetail.put("team", Map.of(
                                "id", detail.getTeam().getId(),
                                "name", detail.getTeam().getName()
                        ));
                    }
                    employmentDetail.put("startDate", detail.getStartDate());
                    employmentDetail.put("workingStatus", detail.getWorkingStatus());

                    member.put("employmentDetail", employmentDetail);
                    return member;
                })
                .collect(Collectors.toList());

        result.put("members", members);
        result.put("memberCount", members.size());

        return result;
    }

    @AfterMapping
    protected void populateMembers(@MappingTarget TeamWithMembersDto target, TeamEntity entity) {
        if (entity == null || entity.getEmploymentDetailEntities() == null) {
            target.setMembers(List.of());
            target.setMemberCount(0);
            return;
        }

        List<UserResponseDto> members = entity.getEmploymentDetailEntities().stream()
                .filter(detail -> detail.getUser() != null)
                .map(detail -> userMapper.entityToDto(detail.getUser()))
                .collect(Collectors.toList());

        target.setMembers(members);
        target.setMemberCount(members.size());
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
