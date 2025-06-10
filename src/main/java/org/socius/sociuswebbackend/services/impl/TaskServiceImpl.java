package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.TaskMapper;
import org.socius.sociuswebbackend.mappers.TeamMapper;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.NotificationType;
import org.socius.sociuswebbackend.model.enums.TaskStatus;
import org.socius.sociuswebbackend.repositories.TaskRepository;
import org.socius.sociuswebbackend.repositories.TeamRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.NotificationService;
import org.socius.sociuswebbackend.services.TaskService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;
    private final UserRepository userRepository;

    @Override
    public TaskResponseDto createTask(TaskRequestDto dto) {
        TaskEntity entity = taskMapper.requestDtoToEntity(dto);
        entity = taskRepository.save(entity);

        if (dto.getAssignedToId() != null) {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserEntity user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

            // Create notification
            NotificationRequestDto notification = NotificationRequestDto.builder()
                    .title("New Task Assigned")
                    .message("You have been assigned a new task: " + entity.getName())
                    .senderId(user.getId()) // System user or task creator
                    .recipientIds(Collections.singletonList(dto.getAssignedToId()))
                    .recipientIds(new ArrayList<>(Collections.singletonList(dto.getAssignedToId())))
                    .type(NotificationType.info)
                    .isUrgent(false)
                    .expiryDate(dto.getDeadline())
                    .build();
            notificationService.createNotification(notification);

            // Send WebSocket message
            TaskResponseDto responseDto = taskMapper.entityToDto(entity);
            messagingTemplate.convertAndSendToUser(
                    dto.getAssignedToId().toString(),
                    "/queue/tasks",
                    responseDto);
        }

        return taskMapper.entityToDto(entity);
    }

    @Override
    public TaskResponseDto updateTaskStatus(UUID taskId, String status) {
        // Tìm task theo ID
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        // Kiểm tra trạng thái mới hợp lệ
        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Valid statuses are: " +
                    Arrays.stream(TaskStatus.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")));
        }

        // Cập nhật trạng thái
        task.setStatus(newStatus);
        task = taskRepository.save(task);

        // Gửi thông báo và WebSocket message nếu có assignedTo
        if (task.getAssignedTo() != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserEntity user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

            NotificationRequestDto notification = NotificationRequestDto.builder()
                    .title("Task Status Updated")
                    .message("Task '" + task.getName() + "' status changed to " + newStatus)
                    .senderId(user.getId())
                    .recipientIds(Collections.singletonList(task.getAssignedTo().getId()))
                    .type(NotificationType.info)
                    .isUrgent(false)
                    .expiryDate(task.getDeadline())
                    .build();
            notificationService.createNotification(notification);

            TaskResponseDto responseDto = taskMapper.entityToDto(task);
            messagingTemplate.convertAndSendToUser(
                    task.getAssignedTo().getId().toString(),
                    "/queue/tasks",
                    responseDto);
        }

        return taskMapper.entityToLimitedDto(task);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Chạy lúc 0:00 mỗi ngày
    @Override
    public void checkAndUpdateOverdueTasks() {
        LocalDate currentDate = LocalDate.now();
        List<TaskStatus> excludedStatuses = Arrays.asList(TaskStatus.completed, TaskStatus.failed);
        List<TaskEntity> overdueTasks = taskRepository.findOverdueTasksNotInStatus(currentDate, excludedStatuses);

        for (TaskEntity task : overdueTasks) {
            task.setStatus(TaskStatus.failed);
            taskRepository.save(task);

            if (task.getAssignedTo() != null) {
                TaskResponseDto responseDto = taskMapper.entityToDto(task);
                messagingTemplate.convertAndSendToUser(
                        task.getAssignedTo().getId().toString(),
                        "/queue/tasks",
                        responseDto);
            }
        }
    }
}