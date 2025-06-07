package org.socius.sociuswebbackend.mappers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeamMappingHelper {

    private final UserMapper userMapper;
    private final EntityMappingUtil entityMappingUtil;

    public Map<String, Object> entityToTeamWithMembers(TeamEntity entity) {
        if (entity == null) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", entity.getId());
        result.put("name", entity.getName());
        result.put("leader", entity.getLeader() != null ? userMapper.toLimitedDto(entity.getLeader()) : null);

        List<Map<String, Object>> members = new ArrayList<>();
        if (entity.getEmploymentDetailEntities() != null) {
            members = entity.getEmploymentDetailEntities().stream()
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
        }

        result.put("members", members);
        result.put("memberCount", members.size());

        return result;
    }

    public TeamEntity requestDtoToEntity(TeamRequestDto dto) {
        if (dto == null) {
            return null;
        }

        UserEntity leader = dto.getLeaderId() != null ?
                entityMappingUtil.mapUserIdToEntity(dto.getLeaderId()) : null;

        return TeamEntity.builder()
                .name(dto.getName())
                .leader(leader)
                .build();
    }

    public void updateEntityFromDto(TeamRequestDto dto, TeamEntity entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getLeaderId() != null) {
            entity.setLeader(entityMappingUtil.mapUserIdToEntity(dto.getLeaderId()));
        }
    }
}