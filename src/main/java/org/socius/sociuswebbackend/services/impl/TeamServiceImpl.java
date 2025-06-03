package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.TaskMapper;
import org.socius.sociuswebbackend.mappers.TeamMapper;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.TeamService;
import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    final private EntityMappingUtil entityMappingUtil;
    final private TaskRepository taskRepository;
    final private TaskMapper taskMapper;

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
    @Transactional
    public TeamResponseDto create(TeamRequestDto requestDto) {
        try {
            if (teamRepository.existsByName(requestDto.getName())) {
                throw new RuntimeException("Team đã tồn tại");
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = auth.getName();

            UserEntity creator = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            UserEntity leader = entityMappingUtil.mapUserIdToEntity(requestDto.getLeaderId());

            TeamEntity team = TeamEntity.builder()
                    .name(requestDto.getName())
                    .leader(leader)
                    .build();

            TeamEntity savedTeam = teamRepository.save(team);

            conversationService.createGroupConversation(
                    savedTeam.getId(),
                    "Team " + savedTeam.getName(),
                    creator.getId(),
                    new HashSet<>()
            );
            return teamMapper.entityToDto(savedTeam);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Không thể tạo team do vi phạm ràng buộc dữ liệu", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo team: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
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
    public void addEmployee(UUID teamId, UUID employeeId) {
        // Tìm team
        teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + teamId));

        // Tìm nhân viên
        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        // Kiểm tra xem nhân viên đã thuộc team chưa
        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        // Kiểm tra xem nhân viên có thuộc team không
        if (employmentDetail.getTeam() != null &&
                employmentDetail.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("Nhân viên đã thuộc team này");
        }

        TeamResponseDto result = addEmployeeToDatabase(teamId, employeeId);

        // Thêm vào group chat trong transaction riêng biệt
        CompletableFuture.runAsync(() -> addToGroupChatSafely(teamId, employeeId));

    }

    @Transactional
    public TeamResponseDto addEmployeeToDatabase(UUID teamId, UUID employeeId) {
        // Logic thêm nhân viên vào database
        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + teamId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        if (employmentDetail.getTeam() != null &&
                !employmentDetail.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("Nhân viên đã thuộc team khác");
        }

        employmentDetail.setTeam(team);
        employmentDetailRepository.save(employmentDetail);

        return teamMapper.entityToDto(team);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void addToGroupChatSafely(UUID teamId, UUID employeeId) {
        try {
            conversationService.addMember(teamId, employeeId);
            logger.info("Đã thêm nhân viên {} vào group chat của team {}", employeeId, teamId);
        } catch (Exception e) {
            logger.error("Không thể thêm nhân viên vào group chat của team: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<TeamResponseDto> addEmployees(UUID teamId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            addEmployee(teamId, employeeId);
        }
        return findAll();
    }

    @Override
    @Transactional
    public List<TeamResponseDto> removeEmployees(UUID teamId, List<UUID> employeeIds) {
        for (UUID employeeId : employeeIds) {
            removeEmployee(teamId, employeeId);
        }
        return findAll();
    }

    @Override
    public void removeEmployee(UUID teamId, UUID employeeId) {
        // Kiểm tra điều kiện trước khi thực hiện
        teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + teamId));

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

        // Kiểm tra xem nhân viên có thuộc team không
        if (employmentDetail.getTeam() == null || !employmentDetail.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("Nhân viên không thuộc team này");
        }

        // Thực hiện cập nhật database trong transaction riêng biệt
        removeEmployeeFromDatabase(teamId, employeeId);

        // Xóa khỏi group chat trong transaction riêng biệt
        CompletableFuture.runAsync(() -> removeFromGroupChatSafely(teamId, employeeId));
    }

    @Transactional
    public void removeEmployeeFromDatabase(UUID teamId, UUID employeeId) {
        try {
            // Tìm team theo ID
            TeamEntity team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy team với ID: " + teamId));

            // Tìm nhân viên theo ID
            UserEntity employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeId));

            // Tìm chi tiết việc làm của nhân viên
            EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUser(employee)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết việc làm cho nhân viên: " + employeeId));

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

            // Xóa nhân viên khỏi team
            employmentDetail.setTeam(null);
            employmentDetailRepository.save(employmentDetail);

            teamMapper.entityToDto(team);
        } catch (Exception e) {
            logger.error("Lỗi khi xóa nhân viên {} khỏi team {}: {}",
                    employeeId, teamId, e.getMessage());
            throw new RuntimeException("Không thể xóa nhân viên khỏi team: " + e.getMessage(), e);
        }
    }

    protected void removeFromGroupChatSafely(UUID teamId, UUID employeeId) {
        try {
            conversationService.removeMember(teamId, employeeId);
            logger.info("Đã xóa nhân viên {} khỏi group chat của team {}", employeeId, teamId);
        } catch (Exception e) {
            logger.error("Không thể xóa nhân viên {} khỏi group chat của team {}: {}",
                    employeeId, teamId, e.getMessage());
            // Không throw exception để không ảnh hưởng đến quá trình chính
        }
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

    @Override
    public Map<String, Object> getTasksByTeamId(UUID teamId, Pageable pageable) {

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with ID: " + teamId));
        List<UUID> memberIds = teamMapper.getMemberIds(team);
        // Kiểm tra danh sách memberIds không rỗng
        if (memberIds == null || memberIds.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("task", new ArrayList<>());
            result.put("totalTaskCount", 0);
            result.put("totalPages", 0);
            result.put("totalElements", 0L);
            return result;
        }

        Page<TaskEntity> taskPage = taskRepository.findByManyAssignedToId(memberIds, pageable);

        List<TaskResponseDto> task = taskPage.getContent().stream()
                .map(taskMapper::entityToLimitedDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("task", task);
        result.put("totalTaskCount", task.size());
        result.put("totalPages", taskPage.getTotalPages());
        result.put("totalElements", taskPage.getTotalElements());

        return result;
    }
}