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

import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<Page<TaskResponseDto>> getTasksByUserId(
            @RequestParam(required = false) UUID userId,
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
}
