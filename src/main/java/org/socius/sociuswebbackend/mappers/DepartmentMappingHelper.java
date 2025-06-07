package org.socius.sociuswebbackend.mappers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.entities.DepartmentEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DepartmentMappingHelper {

    private final UserMapper userMapper;

    public Map<String, Object> entityToDtoWithMembers(DepartmentEntity department) {
        if (department == null) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", department.getId());
        result.put("name", department.getName());
        result.put("description", department.getDescription());

        List<Map<String, Object>> members = department.getEmploymentDetails().stream()
                .filter(detail -> detail.getUser() != null)
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
}