package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;
import java.util.Map;

public interface TaskService {
    Page<TaskResponseDto> getTasksByUserId(UUID userId, Pageable pageable);

    TaskResponseDto createTask(TaskRequestDto taskRequestDto);

    Map<String, Object> getTeamTasks(UUID teamId, Pageable pageable);
}
