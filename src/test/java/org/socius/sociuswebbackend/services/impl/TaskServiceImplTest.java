package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.mappers.TaskMapper;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.TaskStatus;
import org.socius.sociuswebbackend.repositories.TaskRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UserEntity testUser;
    private TaskEntity testTask;
    private TaskRequestDto testTaskRequestDto;
    private TaskResponseDto testTaskResponseDto;

    @BeforeEach
    void setUp() {
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

        UserResponseDto testUserResponseDto = UserResponseDto.builder()
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

        // Mock SecurityContextHolder
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("Tạo task phải gọi taskService và gửi thông báo")
    void createTaskShouldCallTaskService() {
        // Arrange
        when(taskMapper.requestDtoToEntity(testTaskRequestDto)).thenReturn(testTask);
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(testTask);
        when(taskMapper.entityToDto(testTask)).thenReturn(testTaskResponseDto);

        // Act
        taskService.createTask(testTaskRequestDto);

        // Assert
        verify(taskRepository, times(1)).save(testTask);
        verify(notificationService, times(1)).createNotification(any(NotificationRequestDto.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(testUser.getId().toString()),
                eq("/queue/tasks"),
                eq(testTaskResponseDto)
        );
        verify(userRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    @DisplayName("Cập nhật trạng thái task phải gọi taskService và gửi thông báo")
    void updateTaskStatusShouldCallTaskService() {
        // Arrange
        testTask.setStatus(TaskStatus.in_progress); // Cập nhật trạng thái testTask
        testTaskResponseDto.setStatus(TaskStatus.in_progress); // Cập nhật trạng thái testTaskResponseDto

        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(testTask);
        when(taskMapper.entityToDto(any(TaskEntity.class))).thenReturn(testTaskResponseDto);
        when(taskMapper.entityToLimitedDto(any(TaskEntity.class))).thenReturn(testTaskResponseDto);

        // Act
        taskService.updateTaskStatus(testTask.getId(), "in_progress");

        // Assert
        verify(taskRepository, times(1)).findById(testTask.getId());
        verify(taskRepository, times(1)).save(testTask);
        verify(notificationService, times(1)).createNotification(any(NotificationRequestDto.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(testUser.getId().toString()),
                eq("/queue/tasks"),
                eq(testTaskResponseDto)
        );
        verify(userRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    @DisplayName("Cập nhật trạng thái task không làm gì khi trạng thái không hợp lệ")
    void updateTaskStatusShouldDoNothingWhenInvalidStatus() {
        // Arrange
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));

        // Act
        assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTaskStatus(testTask.getId(), "INVALID"));

        // Assert
        verify(taskRepository, times(1)).findById(testTask.getId());
        verify(taskRepository, never()).save(any(TaskEntity.class));
        verify(notificationService, never()).createNotification(any(NotificationRequestDto.class));
        verify(messagingTemplate, never()).convertAndSendToUser(
                anyString(), anyString(), any(TaskResponseDto.class));
    }

    @Test
    @DisplayName("Kiểm tra task quá hạn phải gọi taskService và gửi thông báo")
    void checkAndUpdateOverdueTasksShouldCallTaskService() {
        // Arrange
        LocalDate currentDate = LocalDate.now();
        testTask.setDeadline(currentDate.minusDays(1)); // Quá hạn
        testTask.setStatus(TaskStatus.pending);
        List<TaskEntity> overdueTasks = Arrays.asList(testTask);

        when(taskRepository.findOverdueTasksNotInStatus(eq(currentDate), anyList())).thenReturn(overdueTasks);
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(testTask);
        when(taskMapper.entityToDto(testTask)).thenReturn(testTaskResponseDto);

        // Act
        taskService.checkAndUpdateOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findOverdueTasksNotInStatus(eq(currentDate), anyList());
        verify(taskRepository, times(1)).save(testTask);
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(testUser.getId().toString()),
                eq("/queue/tasks"),
                eq(testTaskResponseDto)
        );
    }
}