package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.TeamMapper;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.entities.EmploymentDetailEntity;
import org.socius.sociuswebbackend.model.entities.EmploymentHistoryEntity;
import org.socius.sociuswebbackend.model.dtos.team.TeamWithMembersDto;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.EmploymentHistoryRepository;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.TeamRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.TeamService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);
    final private TeamRepository teamRepository;
    final private TeamMapper teamMapper;
    final private UserRepository userRepository;
    final private EmploymentDetailRepository employmentDetailRepository;
    final private ConversationService conversationService;
    final private EmploymentHistoryRepository employmentHistoryRepository;

    @Override
    public List<TeamResponseDto> findAll() {
        List<TeamEntity> teams = teamRepository.findAll();
        return teams.stream()
                .map(teamMapper::entityToDto)
                .toList();
    }

    @Override
    public TeamResponseDto findById(UUID id) {
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + id));
        return teamMapper.entityToDto(team);
    }

    @Override
    public TeamResponseDto create(TeamRequestDto requestDto) {
        if (teamRepository.existsByName(requestDto.getName())) {
            throw new RuntimeException("Team đã tồn tại");
        }
        UUID teamId = UUID.randomUUID();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = auth.getName();

        UserEntity creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        TeamEntity team = teamMapper.requestDtoToEntity(requestDto);
        team.setId(teamId);

        team = teamRepository.save(team);

        conversationService.createGroupConversation(
                teamId,
                "Team " + team.getName(),
                creator.getId(),
                new HashSet<>()
        );
        return teamMapper.entityToDto(team);
    }

    @Override
    public TeamResponseDto update(UUID id, TeamRequestDto requestDto) {
        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + id));

        if (!team.getName().equals(requestDto.getName()) &&
                teamRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("Team với tên này đã tồn tại");
        }

        teamMapper.updateEntityFromDto(requestDto, team);
        team = teamRepository.save(team);
        return teamMapper.entityToDto(team);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy team với ID: " + id);
        }

        long count = employmentDetailRepository.countByTeamId(id);
        if (count > 0) {
            throw new IllegalStateException("Không thể xóa team vì vẫn còn " + count + " nhân viên thuộc team này");
        }

        // Xóa group chat của team
        try {
            conversationService.deleteGroupConversation(id);
            logger.info("Đã xóa nhóm trò chuyện của team với ID: {}", id);
        } catch (Exception e) {
            logger.error("Lỗi khi xóa nhóm trò chuyện của team: {}", e.getMessage());
        }

        teamRepository.deleteById(id);
        logger.info("Đã xóa team với ID: {}", id);
    }

    @Override
    public TeamResponseDto addEmployee(UUID teamId, UUID employeeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        UserEntity admin = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Tìm team
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + teamId));

        // Tìm nhân viên
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        // Tìm chi tiết việc làm của nhân viên
        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm của nhân viên"));

        employmentDetail.setTeam(team);
        employmentDetailRepository.save(employmentDetail);

        // Tự động thêm nhân viên vào nhóm trò chuyện của team
        try {
            conversationService.addMember(teamId, employeeId);
            logger.info("Đã thêm nhân viên {} vào nhóm trò chuyện của team {}", employeeId, teamId);
        } catch (Exception e) {
            logger.error("Lỗi khi thêm nhân viên vào nhóm trò chuyện của team: {}", e.getMessage());
        }


        return teamMapper.entityToDto(team);
    }

    @Override
    public List<TeamResponseDto> addEmployees(UUID teamId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            addEmployee(teamId, employeeId);
        }
        return findAll();
    }

    @Override
    public List<TeamResponseDto> removeEmployees(UUID teamId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            removeEmployee(teamId, employeeId);
        }
        return findAll();
    }

    @Override
    public TeamResponseDto removeEmployee(UUID teamId, UUID employeeId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        UserEntity admin = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        // Tìm team
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + teamId));

        // Tìm nhân viên
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        // Tìm chi tiết việc làm của nhân viên
        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm của nhân viên"));

        if (employmentDetail.getTeam() == null || !employmentDetail.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("Nhân viên không thuộc team này");
        }

        // Lưu lịch sử làm việc
        EmploymentHistoryEntity history = EmploymentHistoryEntity.builder()
                .user(employee)
                .position(employmentDetail.getPosition())
                .department(employmentDetail.getDepartment())
                .team(employmentDetail.getTeam())
                .role(employmentDetail.getRole())
                .startDate(employmentDetail.getStartDate())
                .endDate(LocalDate.now())
                .salary(employmentDetail.getSalary())
                .description("Đã rời khỏi team " + team.getName())
                .build();

        employmentHistoryRepository.save(history);

        employmentDetail.setTeam(null);
        employmentDetailRepository.save(employmentDetail);

        // Xóa nhân viên khỏi nhóm trò chuyện của team
        try {
            conversationService.removeMember(teamId, employeeId);
            logger.info("Đã xóa nhân viên {} khỏi nhóm trò chuyện của team {}", employeeId, teamId);
        } catch (Exception e) {
            logger.error("Lỗi khi xóa nhân viên khỏi nhóm trò chuyện của team: {}", e.getMessage());
        }

        return teamMapper.entityToDto(team);
    }

    @Override
    public Map<String, Object> getTeamWithMembers(UUID teamId, Pageable pageable) {
        TeamEntity team = teamRepository.findTeamWithMembers(teamId, pageable)
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));
        return teamMapper.entityToTeamWithMembers(team, pageable);
    }
}
