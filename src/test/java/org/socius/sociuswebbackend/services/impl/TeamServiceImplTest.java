package org.socius.sociuswebbackend.services.impl;

import jakarta.persistence.EntityNotFoundException;
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
import org.socius.sociuswebbackend.mappers.TeamMappingHelper;
import org.socius.sociuswebbackend.mappers.UserMapper;
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
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TeamServiceImplTest {

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMapper teamMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmploymentDetailRepository employmentDetailRepository;

    @Mock
    private ConversationService conversationService;

    @Mock
    private EntityMappingUtil entityMappingUtil;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TeamMappingHelper teamMapperHelper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private TeamServiceImpl teamService;

    private UUID teamId;
    private UUID userId;
    private UUID leaderId;
    private TeamEntity teamEntity;
    private UserEntity userEntity;
    private UserEntity leaderEntity;
    private EmploymentDetailEntity employmentDetailEntity;
    private TeamRequestDto teamRequestDto;
    private TeamResponseDto teamResponseDto;

    @BeforeEach
    void setUp() {
        // Setup static mock
        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);

        // Setup authentication
        when(authentication.getName()).thenReturn("test@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext)
                .thenReturn(securityContext);

        // Initialize test data
        teamId = UUID.randomUUID();
        userId = UUID.randomUUID();
        leaderId = UUID.randomUUID();

        userEntity = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        leaderEntity = UserEntity.builder()
                .id(leaderId)
                .email("leader@example.com")
                .firstName("Leader")
                .lastName("User")
                .build();

        teamEntity = TeamEntity.builder()
                .id(teamId)
                .name("Test Team")
                .leader(leaderEntity)
                .groupChatId(UUID.randomUUID())
                .build();

        employmentDetailEntity = EmploymentDetailEntity.builder()
                .user(leaderEntity)
                .team(teamEntity)
                .build();

        teamRequestDto = TeamRequestDto.builder()
                .name("Test Team")
                .leaderId(leaderId)
                .build();

        teamResponseDto = TeamResponseDto.builder()
                .id(teamId)
                .name("Test Team")
                .leader(userMapper.entityToDto(leaderEntity))
                .build();
    }

    @AfterEach
    void tearDown() {
        if (mockedSecurityContextHolder != null) {
            mockedSecurityContextHolder.close();
        }
    }

    @Test
    @DisplayName("Should return all teams successfully")
    void testFindAll() {
        // Given
        List<TeamEntity> teams = Arrays.asList(teamEntity);
        List<TeamResponseDto> expectedDtos = Arrays.asList(teamResponseDto);

        when(teamRepository.findAll()).thenReturn(teams);
        when(teamMapper.entityToDto(teamEntity)).thenReturn(teamResponseDto);

        // When
        List<TeamResponseDto> result = teamService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDtos.get(0).getName(), result.get(0).getName());
        verify(teamRepository).findAll();
        verify(teamMapper).entityToDto(teamEntity);
    }

    @Test
    @DisplayName("Should return team by id successfully")
    void testFindById() {
        // Given
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamEntity));
        when(teamMapper.entityToDto(teamEntity)).thenReturn(teamResponseDto);

        // When
        TeamResponseDto result = teamService.findById(teamId);

        // Then
        assertNotNull(result);
        assertEquals(teamResponseDto.getName(), result.getName());
        verify(teamRepository).findById(teamId);
        verify(teamMapper).entityToDto(teamEntity);
    }

    @Test
    @DisplayName("Should throw exception when team not found by id")
    void testFindByIdNotFound() {
        // Given
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.findById(teamId));

        assertTrue(exception.getMessage().contains("Không tìm thấy team với ID"));
        verify(teamRepository).findById(teamId);
        verify(teamMapper, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Should create team successfully")
    void testCreate() {
        // Given
        ConversationResponseDto conversationDto = ConversationResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Test Team")
                .build();

        when(teamRepository.existsByName(teamRequestDto.getName())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(entityMappingUtil.mapUserIdToEntity(leaderId)).thenReturn(leaderEntity);
        when(employmentDetailRepository.findByUser(leaderEntity)).thenReturn(Optional.of(employmentDetailEntity));
        when(conversationService.createGroupConversation(anyString(), any(UUID.class), any(HashSet.class)))
                .thenReturn(conversationDto);
        when(teamRepository.save(any(TeamEntity.class))).thenReturn(teamEntity);
        when(teamMapper.entityToDto(teamEntity)).thenReturn(teamResponseDto);

        // When
        TeamResponseDto result = teamService.create(teamRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(teamResponseDto.getName(), result.getName());
        verify(teamRepository).existsByName(teamRequestDto.getName());
        verify(userRepository).findByEmail("test@example.com");
        verify(entityMappingUtil).mapUserIdToEntity(leaderId);
        verify(employmentDetailRepository).findByUser(leaderEntity);
        verify(conversationService).createGroupConversation(anyString(), any(UUID.class), any(HashSet.class));
        verify(teamRepository).save(any(TeamEntity.class));
        verify(teamMapper).entityToDto(teamEntity);
    }

    @Test
    @DisplayName("Should throw exception when team name already exists")
    void testCreateTeamAlreadyExists() {
        // Given
        when(teamRepository.existsByName(teamRequestDto.getName())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.create(teamRequestDto));

        assertEquals("Lỗi khi tạo team: Team đã tồn tại", exception.getMessage());
        verify(teamRepository).existsByName(teamRequestDto.getName());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should throw exception when creator not found")
    void testCreateCreatorNotFound() {
        // Given
        when(teamRepository.existsByName(teamRequestDto.getName())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.create(teamRequestDto));

        assertEquals("Lỗi khi tạo team: Người dùng không tồn tại", exception.getMessage());
        verify(teamRepository).existsByName(teamRequestDto.getName());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when leader employment detail not found")
    void testCreateLeaderEmploymentDetailNotFound() {
        // Given
        when(teamRepository.existsByName(teamRequestDto.getName())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(entityMappingUtil.mapUserIdToEntity(leaderId)).thenReturn(leaderEntity);
        when(employmentDetailRepository.findByUser(leaderEntity)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.create(teamRequestDto));

        assertEquals("Lỗi khi tạo team: Người dùng không phải là thành viên của team", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when data integrity violation occurs")
    void testCreateDataIntegrityViolation() {
        // Given
        ConversationResponseDto conversationDto = ConversationResponseDto.builder()
                .id(UUID.randomUUID())
                .name("Test Team")
                .build();

        when(teamRepository.existsByName(teamRequestDto.getName())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(entityMappingUtil.mapUserIdToEntity(leaderId)).thenReturn(leaderEntity);
        when(employmentDetailRepository.findByUser(leaderEntity)).thenReturn(Optional.of(employmentDetailEntity));

        // Mock conversation service để không bị null
        when(conversationService.createGroupConversation(anyString(), any(UUID.class), any(HashSet.class)))
                .thenReturn(conversationDto);

        // Mock save để ném exception
        when(teamRepository.save(any(TeamEntity.class)))
                .thenThrow(new DataIntegrityViolationException("DB Error"));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> teamService.create(teamRequestDto));

        assertTrue(exception.getMessage().contains("Không thể tạo team do vi phạm ràng buộc dữ liệu"));
    }

    @Test
    @DisplayName("Should throw exception when conversation service returns null")
    void testCreateConversationServiceReturnsNull() {
        // Given
        when(teamRepository.existsByName(teamRequestDto.getName())).thenReturn(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(entityMappingUtil.mapUserIdToEntity(leaderId)).thenReturn(leaderEntity);
        when(employmentDetailRepository.findByUser(leaderEntity)).thenReturn(Optional.of(employmentDetailEntity));
        when(conversationService.createGroupConversation(anyString(), any(UUID.class), any(HashSet.class)))
                .thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> teamService.create(teamRequestDto));

        assertTrue(exception.getMessage().contains("Lỗi khi tạo team"));
    }

    @Test
    @DisplayName("Should update team successfully")
    void testUpdate() {
        // Given
        TeamRequestDto updateRequest = TeamRequestDto.builder()
                .name("Updated Team")
                .leaderId(leaderId)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamEntity));
        when(teamRepository.existsByName(updateRequest.getName())).thenReturn(false);
        when(teamRepository.save(teamEntity)).thenReturn(teamEntity);
        when(teamMapper.entityToDto(teamEntity)).thenReturn(teamResponseDto);
        doNothing().when(teamMapper).updateEntityFromDto(updateRequest, teamEntity);

        // When
        TeamResponseDto result = teamService.update(teamId, updateRequest);

        // Then
        assertNotNull(result);
        verify(teamRepository).findById(teamId);
        verify(teamRepository).existsByName(updateRequest.getName());
        verify(teamMapper).updateEntityFromDto(updateRequest, teamEntity);
        verify(teamRepository).save(teamEntity);
        verify(teamMapper).entityToDto(teamEntity);
    }

    @Test
    @DisplayName("Should update team and conversation name when name changes")
    void testUpdateWithNameChange() {
        // Given
        TeamRequestDto updateRequest = TeamRequestDto.builder()
                .name("Updated Team")
                .leaderId(leaderId)
                .build();

        TeamEntity existingTeam = TeamEntity.builder()
                .id(teamId)
                .name("Old Team")
                .leader(leaderEntity)
                .groupChatId(UUID.randomUUID())
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByName(updateRequest.getName())).thenReturn(false);
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);
        when(teamMapper.entityToDto(existingTeam)).thenReturn(teamResponseDto);
        doNothing().when(teamMapper).updateEntityFromDto(updateRequest, existingTeam);
        doNothing().when(conversationService).updateConversationName(any(UUID.class), anyString());

        // When
        TeamResponseDto result = teamService.update(teamId, updateRequest);

        // Then
        assertNotNull(result);
        verify(conversationService).updateConversationName(existingTeam.getGroupChatId(), updateRequest.getName());
    }

    @Test
    @DisplayName("Should throw exception when team not found for update")
    void testUpdateTeamNotFound() {
        // Given
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.update(teamId, teamRequestDto));

        assertTrue(exception.getMessage().contains("Không tìm thấy team với ID"));
    }

    @Test
    @DisplayName("Should throw exception when updated name already exists")
    void testUpdateNameAlreadyExists() {
        // Given
        TeamEntity existingTeam = TeamEntity.builder()
                .id(teamId)
                .name("Old Team")
                .leader(leaderEntity)
                .build();

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByName(teamRequestDto.getName())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> teamService.update(teamId, teamRequestDto));

        assertEquals("Team với tên này đã tồn tại", exception.getMessage());
    }

    @Test
    @DisplayName("Should delete team successfully")
    void testDelete() {
        // Given
        EmploymentDetailEntity leaderEmploymentDetail = EmploymentDetailEntity.builder()
                .user(leaderEntity)
                .team(teamEntity)
                .build();

        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(employmentDetailRepository.countByTeamId(teamId)).thenReturn(0L);
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamEntity));

        // Thêm mock này để tránh lỗi
        when(employmentDetailRepository.findByUser(leaderEntity))
                .thenReturn(Optional.of(leaderEmploymentDetail));

        when(teamRepository.save(teamEntity)).thenReturn(teamEntity);
        doNothing().when(conversationService).deleteGroupConversation(any(UUID.class));

        // When
        assertDoesNotThrow(() -> teamService.delete(teamId));

        // Then
        verify(teamRepository).existsById(teamId);
        verify(employmentDetailRepository).countByTeamId(teamId);
        verify(teamRepository).findById(teamId);
        verify(employmentDetailRepository).findByUser(leaderEntity); // Thêm verify này
        verify(conversationService).deleteGroupConversation(teamEntity.getGroupChatId());
        verify(teamRepository).save(teamEntity);
    }

    @Test
    @DisplayName("Should throw exception when team not found for delete")
    void testDeleteTeamNotFound() {
        // Given
        when(teamRepository.existsById(teamId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.delete(teamId));

        assertTrue(exception.getMessage().contains("Không tìm thấy team với ID"));
    }

    @Test
    @DisplayName("Should throw exception when team has members")
    void testDeleteTeamHasMembers() {
        // Given
        when(teamRepository.existsById(teamId)).thenReturn(true);
        when(employmentDetailRepository.countByTeamId(teamId)).thenReturn(5L);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> teamService.delete(teamId));

        assertTrue(exception.getMessage().contains("Không thể xóa team vì vẫn còn 5 nhân viên thuộc team này"));
    }

    @Test
    @DisplayName("Should get team with members successfully")
    void testGetTeamWithMembers() {
        // Given
        Pageable pageable = mock(Pageable.class);
        Page<TeamEntity> teamPage = new PageImpl<>(Arrays.asList(teamEntity));
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("team", teamEntity);
        expectedResult.put("members", new ArrayList<>());

        when(teamRepository.findTeamWithMembers(teamId, pageable)).thenReturn(teamPage);
        when(teamMapperHelper.entityToTeamWithMembers(teamEntity)).thenReturn(expectedResult);

        // When
        Map<String, Object> result = teamService.getTeamWithMembers(teamId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(teamRepository).findTeamWithMembers(teamId, pageable);
        verify(teamMapperHelper).entityToTeamWithMembers(teamEntity);
    }

    @Test
    @DisplayName("Should throw exception when team not found for getTeamWithMembers")
    void testGetTeamWithMembersNotFound() {
        // Given
        Pageable pageable = mock(Pageable.class);
        Page<TeamEntity> emptyPage = new PageImpl<>(new ArrayList<>());

        when(teamRepository.findTeamWithMembers(teamId, pageable)).thenReturn(emptyPage);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> teamService.getTeamWithMembers(teamId, pageable));

        assertTrue(exception.getMessage().contains("Team not found with ID"));
    }

    @Test
    @DisplayName("Should get tasks by team id successfully")
    void testGetTasksByTeamId() {
        // Given
        Pageable pageable = mock(Pageable.class);
        List<UUID> memberIds = Arrays.asList(leaderId, userId);
        TaskEntity taskEntity = new TaskEntity();
        TaskResponseDto taskDto = new TaskResponseDto();
        Page<TaskEntity> taskPage = new PageImpl<>(Arrays.asList(taskEntity));

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamEntity));
        when(teamMapper.getMemberIds(teamEntity)).thenReturn(memberIds);
        when(taskRepository.findByManyAssignedToId(memberIds, pageable)).thenReturn(taskPage);
        when(taskMapper.entityToLimitedDto(taskEntity)).thenReturn(taskDto);

        // When
        Map<String, Object> result = teamService.getTasksByTeamId(teamId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("task")).size());
        assertEquals(1, result.get("totalTaskCount"));
        assertEquals(1, result.get("totalPages"));
        assertEquals(1L, result.get("totalElements"));

        verify(teamRepository).findById(teamId);
        verify(teamMapper).getMemberIds(teamEntity);
        verify(taskRepository).findByManyAssignedToId(memberIds, pageable);
        verify(taskMapper).entityToLimitedDto(taskEntity);
    }

    @Test
    @DisplayName("Should return empty result when team has no members")
    void testGetTasksByTeamIdNoMembers() {
        // Given
        Pageable pageable = mock(Pageable.class);

        when(teamRepository.findById(teamId)).thenReturn(Optional.of(teamEntity));
        when(teamMapper.getMemberIds(teamEntity)).thenReturn(new ArrayList<>());

        // When
        Map<String, Object> result = teamService.getTasksByTeamId(teamId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, ((List<?>) result.get("task")).size());
        assertEquals(0, result.get("totalTaskCount"));
        assertEquals(0, result.get("totalPages"));
        assertEquals(0L, result.get("totalElements"));

        verify(teamRepository).findById(teamId);
        verify(teamMapper).getMemberIds(teamEntity);
        verify(taskRepository, never()).findByManyAssignedToId(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when team not found for getTasksByTeamId")
    void testGetTasksByTeamIdTeamNotFound() {
        // Given
        Pageable pageable = mock(Pageable.class);

        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.getTasksByTeamId(teamId, pageable));

        assertTrue(exception.getMessage().contains("Team not found with ID"));
    }
}