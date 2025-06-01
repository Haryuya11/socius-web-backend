package org.socius.sociuswebbackend.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.entities.DepartmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for Department entities and DTOs
 */
@Mapper(componentModel = "spring")
public abstract class DepartmentMapper extends BaseEntityMapper implements
        GenericMapper<DepartmentEntity, DepartmentResponseDto, DepartmentRequestDto> {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract DepartmentResponseDto entityToDto(DepartmentEntity entity);

    @Named("entityToLimitedDto")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract DepartmentResponseDto entityToLimitedDto(DepartmentEntity entity);

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "id", ignore = true)
    public abstract DepartmentEntity requestDtoToEntity(DepartmentRequestDto dto);

    @Override
    public abstract void updateEntityFromDto(DepartmentRequestDto dto, @MappingTarget DepartmentEntity entity);

    public Map<String, Object> entityToDtoWithMembers(DepartmentEntity department, Pageable pageable) {
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
