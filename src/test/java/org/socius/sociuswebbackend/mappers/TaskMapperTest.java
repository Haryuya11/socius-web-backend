package org.socius.sociuswebbackend.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.socius.sociuswebbackend.model.dtos.task.TaskRequestDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.model.entities.TaskEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.model.enums.TaskStatus;
import org.socius.sociuswebbackend.util.EntityMappingUtil;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TaskMapperTest {

    @InjectMocks
    private TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);

    @Mock
    private UserMapper userMapper;

    @Mock
    private EntityMappingUtil entityMappingUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test ánh xạ từ TaskEntity sang TaskResponseDto
     */
    @Test
    void entityToDto_shouldMapCorrectly() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .firstName("Nguyễn")
                .lastName("Văn An")
                .build();
        TaskEntity entity = TaskEntity.builder()
                .id(UUID.randomUUID())
                .name("Task 1")
                .description("Description")
                .deadline(LocalDate.of(2025, 12, 31))
                .status(TaskStatus.pending)
                .assignedTo(user)
                .build();

        UserResponseDto userDto = UserResponseDto.builder()
                .id(userId)
                .firstName("Nguyễn")
                .lastName("Văn An")
                .build();

        when(userMapper.toLimitedDto(user)).thenReturn(userDto);

        // Act
        TaskResponseDto result = taskMapper.entityToDto(entity);

        // Assert
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals("Task 1", result.getName());
        assertEquals("Description", result.getDescription());
        assertEquals(LocalDate.of(2025, 12, 31), result.getDeadline());
        assertEquals(TaskStatus.pending, result.getStatus());
        assertEquals(userDto, result.getAssignedTo());
        assertNull(result.getCreatedAt()); // Ignored in mapping
        assertNull(result.getUpdatedAt()); // Ignored in mapping
    }

    /**
     * Test ánh xạ từ TaskRequestDto sang TaskEntity
     */
    @Test
    void requestDtoToEntity_shouldMapCorrectly() {
        // Arrange
        UUID userId = UUID.randomUUID();
        TaskRequestDto dto = TaskRequestDto.builder()
                .name("Task 1")
                .description("Description")
                .deadline(LocalDate.of(2025, 12, 31))
                .status(TaskStatus.pending)
                .assignedToId(userId)
                .build();

        UserEntity user = UserEntity.builder().id(userId).build();
        when(entityMappingUtil.mapUserIdToEntity(userId)).thenReturn(user);

        // Act
        TaskEntity result = taskMapper.requestDtoToEntity(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Task 1", result.getName());
        assertEquals("Description", result.getDescription());
        assertEquals(LocalDate.of(2025, 12, 31), result.getDeadline());
        assertEquals(TaskStatus.pending, result.getStatus());
        assertEquals(user, result.getAssignedTo());
    }

    /**
     * Test cập nhật TaskEntity từ TaskRequestDto
     */
    @Test
    void updateEntityFromDto_shouldUpdateNonNullFields() {
        // Arrange
        UUID userId = UUID.randomUUID();
        TaskRequestDto dto = TaskRequestDto.builder()
                .name("Updated Task")
                .deadline(LocalDate.of(2025, 12, 31))
                .assignedToId(userId)
                .build();

        TaskEntity entity = TaskEntity.builder()
                .name("Old Task")
                .description("Old Description")
                .deadline(LocalDate.of(2025, 1, 1))
                .status(TaskStatus.pending)
                .build();

        UserEntity user = UserEntity.builder().id(userId).build();
        when(entityMappingUtil.mapUserIdToEntity(userId)).thenReturn(user);

        // Act
        taskMapper.updateEntityFromDto(dto, entity);

        // Assert
        assertEquals("Updated Task", entity.getName());
        assertEquals("Old Description", entity.getDescription()); // Không thay đổi
        assertEquals(LocalDate.of(2025, 12, 31), entity.getDeadline());
        assertEquals(TaskStatus.pending, entity.getStatus()); // Không thay đổi
        assertEquals(user, entity.getAssignedTo());
    }

    /**
     * Test ánh xạ TaskEntity sang TaskResponseDto với entityToLimitedDto
     */
    @Test
    void entityToLimitedDto_shouldMapCorrectly() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .firstName("Lê")
                .lastName("Văn Cường")
                .build();
        TaskEntity entity = TaskEntity.builder()
                .id(UUID.randomUUID())
                .name("Task 2")
                .description("Another Description")
                .deadline(LocalDate.of(2025, 11, 30))
                .status(TaskStatus.in_progress)
                .assignedTo(user)
                .build();

        UserResponseDto userDto = UserResponseDto.builder()
                .id(userId)
                .firstName("Lê")
                .lastName("Văn Cường")
                .build();

        when(userMapper.toLimitedDto(user)).thenReturn(userDto);

        // Act
        TaskResponseDto result = taskMapper.entityToLimitedDto(entity);

        // Assert
        assertNotNull(result);
        assertEquals(entity.getId(), result.getId());
        assertEquals("Task 2", result.getName());
        assertEquals("Another Description", result.getDescription());
        assertEquals(LocalDate.of(2025, 11, 30), result.getDeadline());
        assertEquals(TaskStatus.in_progress, result.getStatus());
        assertEquals(userDto, result.getAssignedTo());
    }
}