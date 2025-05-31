package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.Map;

public interface TaskService {
    Map<String, Object> getTasksByUserId(UUID userId, Pageable pageable);

    Map<String, Object> getTasksByTeamId(UUID teamId, Pageable pageable);

    TaskResponseDto createTask(TaskRequestDto taskRequestDto);
}
