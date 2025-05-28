package org.socius.sociuswebbackend.services.impl;


import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.TaskMapper;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.socius.sociuswebbackend.model.enums.NotificationType;
import org.socius.sociuswebbackend.repositories.TaskRepository;
import org.socius.sociuswebbackend.services.NotificationService;
import org.socius.sociuswebbackend.services.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Page<TaskResponseDto> getTasksByUserId(UUID userId, Pageable pageable) {
        return taskRepository.findByAssignedToId(userId, pageable)
                .map(taskMapper::entityToDto);
    }

    /*@Override
    public TaskResponseDto createTask(TaskRequestDto dto) {
        TaskEntity entity = taskMapper.requestDtoToEntity(dto);
        entity = taskRepository.save(entity);

        if (dto.getAssignedToId() != null) {
            // Create notification
            NotificationRequestDto notification = NotificationRequestDto.builder()
                    .title("New Task Assigned")
                    .message("You have been assigned a new task: " + entity.getName())
                    .senderId(UUID.randomUUID()) // System user or task creator
                    .recipientIds(Collections.singletonList(dto.getAssignedToId()))
                    .recipientIds(new ArrayList<>(Arrays.asList(dto.getAssignedToId())))
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
    }*/
}
