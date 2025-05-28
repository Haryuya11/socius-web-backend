package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.services.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping("/user/{userId}/tasks")
    public ResponseEntity<Page<TaskResponseDto>> getTasksByUserId(
            @PathVariable UUID userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.getTasksByUserId(userId, pageable));
    }

    /*@PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskRequestDto dto
    ) {
        return ResponseEntity.ok(taskService.createTask(dto));
    }*/

    @GetMapping("/team/{teamId}/tasks")
    public ResponseEntity<Map<String, Object>> getTeamTasks(
            @PathVariable UUID teamId,
            Pageable pageable
    ) {
        if (teamId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(taskService.getTeamTasks(teamId, pageable));
    }
}
