package org.socius.sociuswebbackend.services.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.mappers.*;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentDetailResponseDto;
import org.socius.sociuswebbackend.model.dtos.employment.EmploymentHistoryResponseDto;
import org.socius.sociuswebbackend.model.dtos.salary.SalaryHistoryResponseDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.model.enums.WorkingStatus;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.UserService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    final private UserRepository userRepository;
    final private UserMapper userMapper;
    final private EmploymentDetailRepository employmentDetailRepository;
    final private EmploymentHistoryRepository employmentHistoryRepository;
    final private EmploymentHistoryMapper employmentHistoryMapper;
    final private SalaryHistoryRepository salaryHistoryRepository;
    final private SalaryHistoryMapper salaryHistoryMapper;
    final private EmploymentDetailMapper employmentDetailMapper;
    final private TaskRepository taskRepository;
    final private TaskMapper taskMapper;

    @Override
    public UserResponseDto findById(UUID userId) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);
        if (userEntity.isEmpty()) {
            return null;
        }
        UserEntity user = userEntity.get();
        return userMapper.entityToDto(user);
    }

    @Override
    public UserResponseDto getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        String userKey = RedisKeyBuilder.userIdAttributeKey();
        UUID userId = (UUID) session.getAttribute(userKey);
        if (userId == null) {
            return null;
        }

        Optional<UserEntity> userOptional = userRepository.findById(userId);
        return userOptional.map(userMapper::entityToDto).orElse(null);
    }

    @Override
    public List<UserResponseDto> getActiveUsersNotInAnyTeam(HttpServletRequest request) {
        List<UserEntity> users = userRepository.findUsersNotInAnyTeam();
        return users.stream()
                .filter(user -> employmentDetailRepository.findByUserId(user.getId())
                        .map(detail -> detail.getWorkingStatus() == WorkingStatus.active)
                        .orElse(false))
                .map(userMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getEmploymentHistory(UUID userId, Pageable pageable) {
        UserEntity user = UserEntity.builder().id(userId).build();
        Page<EmploymentHistoryEntity> historyPage = employmentHistoryRepository.findByUser(user, pageable);

        List<EmploymentHistoryResponseDto> history = historyPage.getContent().stream()
                .map(employmentHistoryMapper::entityToLimitedDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("history", history);
        result.put("historyCount", history.size());
        result.put("totalPages", historyPage.getTotalPages());
        result.put("totalElements", historyPage.getTotalElements());

        return result;
    }

    @Override
    public Map<String, Object> getSalaryHistory(UUID userId, Pageable pageable) {
        UserEntity user = UserEntity.builder().id(userId).build();
        Page<SalaryHistoryEntity> salaryPage = salaryHistoryRepository.findByUser(user, pageable);

        List<SalaryHistoryResponseDto> salaryHistory = salaryPage.getContent().stream()
                .map(salaryHistoryMapper::entityToLimitedDto)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("salaryHistory", salaryHistory);
        result.put("historyCount", salaryHistory.size());
        result.put("totalPages", salaryPage.getTotalPages());
        result.put("totalElements", salaryPage.getTotalElements());

        return result;
    }

    @Override
    public Map<String, Object> getEmploymentDetailByUserId(UUID userId) {
        EmploymentDetailEntity employmentDetail = employmentDetailRepository.findByUserId(userId)
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        if (employmentDetail == null) {
            result.put("employmentDetail", null);
            return result;
        }

        EmploymentDetailResponseDto employmentDetailDto = employmentDetailMapper.entityToLimitedDto(employmentDetail);
        result.put("employmentDetail", employmentDetailDto);

        return result;
    }

    @Override
    public List<UserResponseDto> getActiveUsersNotInAnyDepartment(HttpServletRequest request) {
        List<UserEntity> users = userRepository.findUsersNotInAnyDepartment();
        return users.stream()
                .filter(user -> employmentDetailRepository.findByUserId(user.getId())
                        .map(detail -> detail.getWorkingStatus() == WorkingStatus.active)
                        .orElse(false))
                .map(userMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDto> getActiveUsersNotInAnyPosition(HttpServletRequest request) {
        List<UserEntity> users = userRepository.findUsersNotInAnyPosition();
        return users.stream()
                .filter(user -> employmentDetailRepository.findByUserId(user.getId())
                        .map(detail -> detail.getWorkingStatus() == WorkingStatus.active)
                        .orElse(false))
                .map(userMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getTasksByUserId(UUID userId, Pageable pageable) {
        Page<TaskEntity> taskPage = taskRepository.findByAssignedToId(userId, pageable);

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
