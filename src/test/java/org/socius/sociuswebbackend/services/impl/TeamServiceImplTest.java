package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.mappers.TaskMapper;
import org.socius.sociuswebbackend.mappers.TeamMapper;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.task.TaskResponseDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamRequestDto;
import org.socius.sociuswebbackend.model.dtos.team.TeamResponseDto;
import org.socius.sociuswebbackend.model.entities.*;
import org.socius.sociuswebbackend.repositories.*;
import org.socius.sociuswebbackend.services.ConversationService;
import org.socius.sociuswebbackend.util.EntityMappingUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EmploymentDetailRepository employmentDetailRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private ConversationService conversationService;

    @Mock
    private EntityMappingUtil entityMappingUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TeamServiceImpl teamService;

    private MockedStatic<SecurityContextHolder> securityContextHolder;

    private TeamEntity testTeam;
    private TeamRequestDto testTeamRequestDto;
    private TeamResponseDto testTeamResponseDto;
    private UserEntity testUser;
    private TaskEntity testTask;
    private TaskResponseDto testTaskResponseDto;

    @BeforeEach
    void setUp() {
        securityContextHolder = mockStatic(SecurityContextHolder.class);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        // Test User
        testUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        // Test Team Entity
        testTeam = TeamEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Team")
                .leader(testUser)
                .groupChatId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Test Team Request DTO
        testTeamRequestDto = TeamRequestDto.builder()
                .name("Test Team")
                .leaderId(testUser.getId())
                .build();

        // Test Team Response DTO
        testTeamResponseDto = TeamResponseDto.builder()
                .id(testTeam.getId())
                .name("Test Team")
                .leader(null) // Simplified for testing
                .createdAt(testTeam.getCreatedAt())
                .updatedAt(testTeam.getUpdatedAt())
                .build();

        // Test Task
        testTask = TaskEntity.builder()
                .id(UUID.randomUUID())
                .description("Test Description")
                .assignedTo(testUser)
                .build();

        // Test Task Response DTO
        testTaskResponseDto = TaskResponseDto.builder()
                .id(testTask.getId())
                .description("Test Description")
                .build();
    }

    @AfterEach
    void tearDown() {
        securityContextHolder.close();
    }

    @Test
    @DisplayName("Tìm tất cả teams thành công")
    void findAllShouldReturnAllTeams() {
        // Given
        List<TeamEntity> teams = Collections.singletonList(testTeam);
        when(teamRepository.findAll()).thenReturn(teams);
        when(teamMapper.entityToDto(testTeam)).thenReturn(testTeamResponseDto);

        // When
        List<TeamResponseDto> result = teamService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTeamResponseDto.getName(), result.getFirst().getName());
        verify(teamRepository).findAll();
        verify(teamMapper).entityToDto(testTeam);
    }

    @Test
    @DisplayName("Tìm team theo ID thành công")
    void findByIdShouldReturnTeam() {
        // Given
        when(teamRepository.findById(testTeam.getId())).thenReturn(Optional.of(testTeam));
        when(teamMapper.entityToDto(testTeam)).thenReturn(testTeamResponseDto);

        // When
        TeamResponseDto result = teamService.findById(testTeam.getId());

        // Then
        assertNotNull(result);
        assertEquals(testTeamResponseDto.getName(), result.getName());
        verify(teamRepository).findById(testTeam.getId());
        verify(teamMapper).entityToDto(testTeam);
    }

    @Test
    @DisplayName("Tìm team theo ID không tồn tại nên throw exception")
    void findByIdShouldThrowExceptionWhenNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(teamRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> teamService.findById(nonExistentId)
        );

        assertEquals("Không tìm thấy team với ID: " + nonExistentId, exception.getMessage());
        verify(teamRepository).findById(nonExistentId);
        verify(teamMapper, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Tạo team thành công")
    void createShouldReturnCreatedTeam() {
        // Given
        ConversationResponseDto mockConversation = ConversationResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Test Team Group Chat")
                .build();

        when(teamRepository.existsByName(testTeamRequestDto.getName())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(entityMappingUtil.mapUserIdToEntity(testTeamRequestDto.getLeaderId())).thenReturn(testUser);
        when(conversationService.createGroupConversation(anyString(), any(UUID.class), any())).thenReturn(mockConversation);
        when(teamRepository.save(any(TeamEntity.class))).thenReturn(testTeam);
        when(teamMapper.entityToDto(testTeam)).thenReturn(testTeamResponseDto);

        // When
        TeamResponseDto result = teamService.create(testTeamRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(testTeamResponseDto.getName(), result.getName());
        verify(teamRepository).existsByName(testTeamRequestDto.getName());
        verify(teamRepository).save(any(TeamEntity.class));
        verify(conversationService).createGroupConversation(anyString(), any(UUID.class), any());
    }

    @Test
    @DisplayName("Tạo team với tên đã tồn tại nên throw exception")
    void createShouldThrowExceptionWhenNameExists() {
        // Given
        when(teamRepository.existsByName(testTeamRequestDto.getName())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> teamService.create(testTeamRequestDto)
        );

        assertEquals("Team đã tồn tại", exception.getMessage());
        verify(teamRepository).existsByName(testTeamRequestDto.getName());
        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo team với lỗi database constraint nên throw IllegalArgumentException")
    void createShouldThrowIllegalArgumentExceptionOnDataIntegrityViolation() {
        // Given
        when(teamRepository.existsByName(testTeamRequestDto.getName())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(entityMappingUtil.mapUserIdToEntity(testTeamRequestDto.getLeaderId())).thenReturn(testUser);
        when(conversationService.createGroupConversation(anyString(), any(UUID.class), any()))
                .thenThrow(new DataIntegrityViolationException("Database constraint violation"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> teamService.create(testTeamRequestDto)
        );

        assertEquals("Không thể tạo team do vi phạm ràng buộc dữ liệu", exception.getMessage());
    }

    @Test
    @DisplayName("Cập nhật team thành công")
    void updateShouldReturnUpdatedTeam() {
        // Given
        TeamRequestDto updateDto = TeamRequestDto.builder()
                .name("Updated Team")
                .leaderId(testUser.getId())
                .build();

        when(teamRepository.findById(testTeam.getId())).thenReturn(Optional.of(testTeam));
        when(teamRepository.existsByName(updateDto.getName())).thenReturn(false);
        when(teamRepository.save(testTeam)).thenReturn(testTeam);
        when(teamMapper.entityToDto(testTeam)).thenReturn(testTeamResponseDto);

        // When
        TeamResponseDto result = teamService.update(testTeam.getId(), updateDto);

        // Then
        assertNotNull(result);
        verify(teamRepository).findById(testTeam.getId());
        verify(teamRepository).save(testTeam);
        verify(conversationService).updateConversationName(testTeam.getGroupChatId(), updateDto.getName());
    }

    @Test
    @DisplayName("Cập nhật team với tên đã tồn tại nên throw exception")
    void updateShouldThrowExceptionWhenNameExists() {
        // Given
        TeamRequestDto updateDto = TeamRequestDto.builder()
                .name("Existing Team")
                .leaderId(testUser.getId())
                .build();

        when(teamRepository.findById(testTeam.getId())).thenReturn(Optional.of(testTeam));
        when(teamRepository.existsByName(updateDto.getName())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> teamService.update(testTeam.getId(), updateDto)
        );

        assertEquals("Team với tên này đã tồn tại", exception.getMessage());
        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cập nhật team không tồn tại nên throw exception")
    void updateShouldThrowExceptionWhenTeamNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(teamRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> teamService.update(nonExistentId, testTeamRequestDto)
        );

        assertEquals("Không tìm thấy team với ID: " + nonExistentId, exception.getMessage());
        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xóa team thành công")
    void deleteShouldRemoveTeam() {
        // Given
        when(teamRepository.existsById(testTeam.getId())).thenReturn(true);
        when(employmentDetailRepository.countByTeamId(testTeam.getId())).thenReturn(0L);

        // When
        assertDoesNotThrow(() -> teamService.delete(testTeam.getId()));

        // Then
        verify(teamRepository).existsById(testTeam.getId());
        verify(employmentDetailRepository).countByTeamId(testTeam.getId());
        verify(conversationService).deleteGroupConversation(testTeam.getId());
        verify(teamRepository).deleteById(testTeam.getId());
    }

    @Test
    @DisplayName("Xóa team không tồn tại nên throw exception")
    void deleteShouldThrowExceptionWhenTeamNotFound() {
        // Given
        when(teamRepository.existsById(testTeam.getId())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> teamService.delete(testTeam.getId())
        );

        assertEquals("Không tìm thấy team với ID: " + testTeam.getId(), exception.getMessage());
        verify(teamRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Xóa team đang có nhân viên nên throw exception")
    void deleteShouldThrowExceptionWhenTeamHasMembers() {
        // Given
        when(teamRepository.existsById(testTeam.getId())).thenReturn(true);
        when(employmentDetailRepository.countByTeamId(testTeam.getId())).thenReturn(3L);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> teamService.delete(testTeam.getId())
        );

        assertEquals("Không thể xóa team vì vẫn còn 3 nhân viên thuộc team này", exception.getMessage());
        verify(teamRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Lấy team với members thành công")
    void getTeamWithMembersShouldReturnTeamAndMembers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TeamEntity> mockPage = new PageImpl<>(Collections.singletonList(testTeam));

        // Mock phương thức đúng mà implementation sử dụng
        when(teamRepository.findTeamWithMembers(testTeam.getId(), pageable)).thenReturn(mockPage);
        when(teamMapper.entityToTeamWithMembers(testTeam, pageable)).thenReturn(new HashMap<>());

        // When
        Map<String, Object> result = teamService.getTeamWithMembers(testTeam.getId(), pageable);

        // Then
        assertNotNull(result);
        verify(teamRepository).findTeamWithMembers(testTeam.getId(), pageable);
        verify(teamMapper).entityToTeamWithMembers(testTeam, pageable);
    }

    @Test
    @DisplayName("Lấy team với members khi team không tồn tại nên throw exception")
    void getTeamWithMembersShouldThrowExceptionWhenTeamNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        // Mock trả về page rỗng thay vì null
        Page<TeamEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        when(teamRepository.findTeamWithMembers(nonExistentId, pageable)).thenReturn(emptyPage);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, // Đổi thành IllegalArgumentException theo implementation
                () -> teamService.getTeamWithMembers(nonExistentId, pageable)
        );

        assertEquals("Team not found with ID: " + nonExistentId, exception.getMessage());
        verify(teamMapper, never()).entityToTeamWithMembers(any(), any());
    }

    @Test
    @DisplayName("Lấy tasks của team thành công")
    void getTasksByTeamIdShouldReturnTasks() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<UUID> memberIds = Collections.singletonList(testUser.getId());
        Page<TaskEntity> taskPage = new PageImpl<>(Collections.singletonList(testTask));

        when(teamRepository.findById(testTeam.getId())).thenReturn(Optional.of(testTeam));
        when(teamMapper.getMemberIds(testTeam)).thenReturn(memberIds);
        when(taskRepository.findByManyAssignedToId(memberIds, pageable)).thenReturn(taskPage);
        when(taskMapper.entityToLimitedDto(testTask)).thenReturn(testTaskResponseDto);

        // When
        Map<String, Object> result = teamService.getTasksByTeamId(testTeam.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("task")).size());
        assertEquals(1, result.get("totalTaskCount"));
        assertEquals(1, result.get("totalPages"));
        assertEquals(1L, result.get("totalElements"));
        verify(teamRepository).findById(testTeam.getId());
        verify(taskRepository).findByManyAssignedToId(memberIds, pageable);
    }

    @Test
    @DisplayName("Lấy tasks của team không có members nên trả về empty")
    void getTasksByTeamIdShouldReturnEmptyWhenNoMembers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(teamRepository.findById(testTeam.getId())).thenReturn(Optional.of(testTeam));
        when(teamMapper.getMemberIds(testTeam)).thenReturn(new ArrayList<>());

        // When
        Map<String, Object> result = teamService.getTasksByTeamId(testTeam.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, ((List<?>) result.get("task")).size());
        assertEquals(0, result.get("totalTaskCount"));
        assertEquals(0, result.get("totalPages"));
        assertEquals(0L, result.get("totalElements"));
        verify(taskRepository, never()).findByManyAssignedToId(any(), any());
    }

    @Test
    @DisplayName("Lấy tasks của team không tồn tại nên throw exception")
    void getTasksByTeamIdShouldThrowExceptionWhenTeamNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(teamRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> teamService.getTasksByTeamId(nonExistentId, pageable)
        );

        assertEquals("Team not found with ID: " + nonExistentId, exception.getMessage());
        verify(taskRepository, never()).findByManyAssignedToId(any(), any());
    }
}