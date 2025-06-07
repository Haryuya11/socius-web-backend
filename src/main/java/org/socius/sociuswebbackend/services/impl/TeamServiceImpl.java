package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.TaskMapper;
import org.socius.sociuswebbackend.mappers.TeamMapper;
import org.socius.sociuswebbackend.mappers.TeamMappingHelper;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.TaskRepository;
import org.socius.sociuswebbackend.repositories.TeamRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.services.TeamService;
import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    final private EntityMappingUtil entityMappingUtil;
    final private TaskRepository taskRepository;
    final private TaskMapper taskMapper;
    final private TeamMappingHelper teamMapperHelper;

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

            HashSet<UUID> initialMembers = new HashSet<>();
            initialMembers.add(leader.getId());

            ConversationResponseDto groupChat = conversationService.createGroupConversation(
                    team.getName(),
                    creator.getId(),
                    initialMembers
            );

            team.setGroupChatId(groupChat.getId());

            TeamEntity savedTeam = teamRepository.save(team);

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

        String oldName = team.getName();
        boolean nameChanged = !oldName.equals(requestDto.getName());

        if (nameChanged && teamRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("Team với tên này đã tồn tại");
        }

        teamMapper.updateEntityFromDto(requestDto, team);
        team = teamRepository.save(team);

        // Cập nhật tên group chat nếu tên team thay đổi
        if (nameChanged && team.getGroupChatId() != null) {
            try {
                conversationService.updateConversationName(team.getGroupChatId(), requestDto.getName());
                logger.info("Đã cập nhật tên group chat của team {} từ '{}' sang '{}'",
                        id, oldName, requestDto.getName());
            } catch (Exception e) {
                logger.error("Lỗi khi cập nhật tên group chat của team {}: {}", id, e.getMessage());
                throw new RuntimeException("Lỗi khi cập nhật tên nhóm trò chuyện của team: " + e.getMessage(), e);
            }
        }

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
    public Map<String, Object> getTeamWithMembers(UUID teamId, Pageable pageable) {
        TeamEntity team = teamRepository.findTeamWithMembers(teamId, pageable)
                .getContent()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + teamId));
        return teamMapperHelper.entityToTeamWithMembers(team);
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