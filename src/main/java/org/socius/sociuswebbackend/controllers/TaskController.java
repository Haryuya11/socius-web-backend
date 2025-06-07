package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.services.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    /**
     * Tạo một task mới
     *
     * @param dto Đối tượng chứa thông tin task cần tạo
     * @return Thông tin task vừa tạo, hoặc 400 nếu dữ liệu không hợp lệ
     */
    @PostMapping("/create")
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskRequestDto dto
    ) {
        return ResponseEntity.ok(taskService.createTask(dto));
    }

    /**
     * Cập nhật trạng thái của một task
     *
     * @param taskId ID của task cần cập nhật
     * @param status Trạng thái mới của task
     * @return Thông tin task đã cập nhật, hoặc 400 nếu taskId/status không hợp lệ
     */
    @PatchMapping("/{taskId}/update-status/{status}")
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
