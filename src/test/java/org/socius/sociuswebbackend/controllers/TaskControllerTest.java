package org.socius.sociuswebbackend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.TaskStatus;
import org.socius.sociuswebbackend.services.TaskService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TaskRequestDto testTaskRequestDto;
    private TaskResponseDto testTaskResponseDto;
    private UserResponseDto testUserResponseDto;
    private UserEntity testUser;
    private TaskEntity testTask;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Đăng ký JavaTimeModule
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();

        testUser = UserEntity.builder()
                .id(UUID.fromString("eb39ff27-0b69-4529-96e8-03e8fb582d05"))
                .email("user@example.com")
                .firstName("Nguyễn")
                .lastName("Văn An")
                .build();

        testTask = TaskEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Task")
                .description("Test task description")
                .deadline(LocalDate.of(2025, 12, 31))
                .status(TaskStatus.pending)
                .assignedTo(testUser)
                .build();

        testUserResponseDto = UserResponseDto.builder()
                .id(testUser.getId())
                .firstName("Nguyễn")
                .lastName("Văn An")
                .build();

        testTaskRequestDto = TaskRequestDto.builder()
                .name("Test Task")
                .description("Test task description")
                .deadline(LocalDate.of(2025, 12, 31))
                .status(TaskStatus.pending)
                .assignedToId(testUser.getId())
                .build();

        testTaskResponseDto = TaskResponseDto.builder()
                .id(testTask.getId())
                .name("Test Task")
                .description("Test task description")
                .deadline(LocalDate.of(2025, 12, 31))
                .status(TaskStatus.pending)
                .assignedTo(testUserResponseDto)
                .build();
    }

    @Test
    @DisplayName("Tạo task phải gọi taskService")
    void createTaskShouldCallTaskService() throws Exception {
        // Arrange
        when(taskService.createTask(any(TaskRequestDto.class))).thenReturn(testTaskResponseDto);

        // Act
        mockMvc.perform(post("/api/task/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTaskRequestDto)))
                .andExpect(status().isOk());

        // Assert
        verify(taskService, times(1)).createTask(any(TaskRequestDto.class));
    }

    @Test
    @DisplayName("Tạo task không làm gì khi dữ liệu không hợp lệ")
    void createTaskShouldDoNothingWhenDataInvalid() throws Exception {
        // Arrange
        TaskRequestDto invalidDto = TaskRequestDto.builder()
                .name("") // Tên rỗng
                .description("Test task description")
                .deadline(LocalDate.of(2025, 12, 31))
                .status(TaskStatus.pending)
                .assignedToId(testUser.getId())
                .build();

        // Act
        mockMvc.perform(post("/api/task/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        // Assert
        verify(taskService, never()).createTask(any(TaskRequestDto.class));
    }

    @Test
    @DisplayName("Cập nhật trạng thái task phải gọi taskService")
    void updateTaskStatusShouldCallTaskService() throws Exception {
        // Arrange
        testTaskResponseDto.setStatus(TaskStatus.in_progress);
        when(taskService.updateTaskStatus(eq(testTask.getId()), eq("in_progress"))).thenReturn(testTaskResponseDto);

        // Act
        mockMvc.perform(patch("/api/task/{taskId}/update-status/in_progress", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assert
        verify(taskService, times(1)).updateTaskStatus(eq(testTask.getId()), eq("in_progress"));
    }

    @Test
    @DisplayName("Cập nhật trạng thái task không làm gì khi trạng thái không hợp lệ")
    void updateTaskStatusShouldDoNothingWhenStatusInvalid() throws Exception {
        // Arrange
        when(taskService.updateTaskStatus(eq(testTask.getId()), eq("INVALID")))
                .thenThrow(new IllegalArgumentException("Invalid status"));

        // Act
        mockMvc.perform(patch("/api/task/{taskId}/update-status/INVALID", testTask.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Assert
        verify(taskService, times(1)).updateTaskStatus(eq(testTask.getId()), eq("INVALID"));
    }
}