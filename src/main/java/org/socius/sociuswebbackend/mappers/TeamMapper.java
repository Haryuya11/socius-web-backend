package org.socius.sociuswebbackend.mappers;

import org.mapstruct.*;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamWithMembersDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.TaskRepository;
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
    protected TaskRepository taskRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public abstract TeamResponseDto entityToDto(TeamEntity entity);

    @Named("entityToLimitedDto")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "leader", ignore = true)
    public abstract TeamResponseDto entityToLimitedDto(TeamEntity entity);

    @Override
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

    public Map<String, Object> entityToTeamWithTasks(TeamEntity entity, org.springframework.data.domain.Pageable pageable) {
        if (entity == null) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        result.put("name", entity.getName());
        result.put("leader", entity.getLeader() != null ? userMapper.entityToDto(entity.getLeader()) : null);

        List<Map<String, Object>> members = entity.getEmploymentDetailEntities().stream()
                .filter(detail -> detail.getUser() != null)
                .map(detail -> {
                    UserEntity user = detail.getUser();
                    UserResponseDto userDto = userMapper.entityToDto(user);
                    List<TaskResponseDto> tasks = taskRepository.findByAssignedToId(user.getId(), pageable)
                            .getContent()
                            .stream()
                            .map(taskMapper::entityToDto)
                            .collect(Collectors.toList());

                    Map<String, Object> memberData = new HashMap<>();
                    memberData.put("member", userDto);
                    memberData.put("tasks", tasks);
                    return memberData;
                })
                .collect(Collectors.toList());

        result.put("members", members);
        result.put("memberCount", members.size());

        return result;
    }

    public Map<String, Object> entityToMemberWithTasks(TeamEntity entity, UUID memberId, org.springframework.data.domain.Pageable pageable) {
        if (entity == null || entity.getEmploymentDetailEntities() == null) {
            return Map.of();
        }

        EmploymentDetailEntity employmentDetail = entity.getEmploymentDetailEntities().stream()
                .filter(detail -> detail.getUser() != null && detail.getUser().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Member not found in team or invalid member ID: " + memberId));

        UserEntity user = employmentDetail.getUser();
        UserResponseDto userDto = userMapper.entityToDto(user);
        List<TaskResponseDto> tasks = taskRepository.findByAssignedToId(user.getId(), pageable)
                .getContent()
                .stream()
                .map(taskMapper::entityToDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("member", userDto);
        result.put("tasks", tasks);

        return result;
    }

    public Map<String, Object> entityToTeamWithMembers(TeamEntity entity, org.springframework.data.domain.Pageable pageable) {
        if (entity == null) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        result.put("name", entity.getName());
        result.put("leader", entity.getLeader() != null ? userMapper.toLimitedDto(entity.getLeader()) : null);

        List<UserResponseDto> members = entity.getEmploymentDetailEntities().stream()
                .filter(detail -> detail.getUser() != null)
                .map(detail -> userMapper.toLimitedDto(detail.getUser()))
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
}
