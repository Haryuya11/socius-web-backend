package org.socius.sociuswebbackend.mappers;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.socius.sociuswebbackend.model.dtos.position.PositionRequestDto;
import org.socius.sociuswebbackend.model.dtos.position.PositionResponseDto;
import org.socius.sociuswebbackend.model.entities.PositionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for Position entities and DTOs
 */
@Mapper(componentModel = "spring")
public abstract class PositionMapper extends BaseEntityMapper implements
        GenericMapper<PositionEntity, PositionResponseDto, PositionRequestDto> {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract PositionResponseDto entityToDto(PositionEntity entity);

    @Override
    public abstract PositionEntity requestDtoToEntity(PositionRequestDto dto);

    @Override
    public abstract void updateEntityFromDto(PositionRequestDto dto, @MappingTarget PositionEntity entity);

    public Map<String, Object> entityToDtoWithMembers(PositionEntity position, Pageable pageable) {
        if( position == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", position.getId());
        result.put("name", position.getName());
        result.put("description", position.getDescription());

        List<Map<String, Object>> members = position.getEmploymentDetails().stream()
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
                        employmentDetail.put("", Map.of(
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
