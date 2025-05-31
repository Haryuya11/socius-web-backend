package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.mappers.TeamMapper;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.entities.TeamEntity;
import org.socius.sociuswebbackend.repositories.TeamRepository;
import org.socius.sociuswebbackend.services.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/user/{userId}/tasks")
    public ResponseEntity<Map<String, Object>> getTasksByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = taskService.getTasksByUserId(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/task/create")
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskRequestDto dto
    ) {
        return ResponseEntity.ok(taskService.createTask(dto));
    }

    @GetMapping("/team/{teamId}/tasks")
    public ResponseEntity<Map<String, Object>> getTeamTasks(
            @PathVariable UUID teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> response = taskService.getTasksByTeamId(teamId, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/task/{taskId}/update-status/{status}")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @PathVariable UUID taskId,
            @PathVariable String status) {
        if (taskId == null || status == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            TaskResponseDto response = taskService.updateTaskStatus(taskId, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
