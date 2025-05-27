package org.socius.sociuswebbackend.services.impl;


import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.TaskMapper;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.socius.sociuswebbackend.repositories.TaskRepository;
import org.socius.sociuswebbackend.services.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    public Page<TaskResponseDto> getTasksByUserId(UUID userId, Pageable pageable) {
        return taskRepository.findByAssignedToId(userId, pageable)
                .map(taskMapper::entityToDto);
    }

    /*@Override
    public TaskResponseDto createTask(TaskRequestDto dto){
        TaskEntity entity = taskMapper.requestDtoToEntity(dto);
        entity = taskRepository.save(entity);

        return taskMapper.entityToDto(entity);
    }*/
}
