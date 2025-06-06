package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.mappers.DepartmentMapper;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentRequestDto;
import org.socius.sociuswebbackend.model.dtos.department.DepartmentResponseDto;
import org.socius.sociuswebbackend.model.entities.DepartmentEntity;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.DepartmentRepository;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConversationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private ConversationService conversationService;

    @Mock
    private EmploymentDetailRepository employmentDetailRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private MockedStatic<SecurityContextHolder> securityContextHolder;

    @BeforeEach
    void setUp() {
        securityContextHolder = mockStatic(SecurityContextHolder.class);
        securityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        UserEntity mockUser = UserEntity.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

    }

    @AfterEach
    void tearDown() {
        if (securityContextHolder != null) {
            securityContextHolder.close();
            securityContextHolder = null;
        }
    }

    @Test
    @DisplayName("Tạo phòng ban thành công")
    void createDepartmentSuccessfully() {
        // Given
        DepartmentRequestDto requestDto = DepartmentRequestDto.builder()
                .name("IT Department")
                .description("Information Technology Department")
                .build();

        DepartmentEntity departmentEntity = DepartmentEntity.builder()
                .id(UUID.randomUUID())
                .name("IT Department")
                .description("Information Technology Department")
                .build();

        DepartmentResponseDto expectedResponse = DepartmentResponseDto.builder()
                .id(departmentEntity.getId())
                .name("IT Department")
                .description("Information Technology Department")
                .build();

        ConversationResponseDto mockConversation = ConversationResponseDto.builder()
                .id(UUID.randomUUID())
                .name("IT Department Group Chat")
                .build();

        // Mock các dependencies
        when(departmentRepository.existsByName("IT Department")).thenReturn(false);
        when(departmentMapper.requestDtoToEntity(requestDto)).thenReturn(departmentEntity);
        when(departmentRepository.save(any(DepartmentEntity.class))).thenReturn(departmentEntity);
        when(departmentMapper.entityToDto(departmentEntity)).thenReturn(expectedResponse);
        when(conversationService.createGroupConversation(anyString(), any(UUID.class), any()))
                .thenReturn(mockConversation);

        // When
        DepartmentResponseDto result = departmentService.create(requestDto);

        // Then
        assertNotNull(result);
        assertEquals("IT Department", result.getName());
        assertEquals("Information Technology Department", result.getDescription());

        verify(departmentRepository).existsByName("IT Department");
        verify(departmentRepository).save(any(DepartmentEntity.class));
        verify(conversationService).createGroupConversation(anyString(), any(UUID.class), any());
    }

    @Test
    @DisplayName("Lỗi khi tạo phòng ban với tên đã tồn tại")
    void failToCreateDepartmentWithExistingName() {
        // Given
        DepartmentRequestDto requestDto = DepartmentRequestDto.builder()
                .name("IT Department")
                .description("Information Technology Department")
                .build();

        when(departmentRepository.existsByName("IT Department")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> departmentService.create(requestDto)
        );

        // Sửa message từ "Phòng ban với tên này đã tồn tại" thành "Phòng ban đã tồn tại"
        assertEquals("Phòng ban đã tồn tại", exception.getMessage());
        verify(departmentRepository).existsByName("IT Department");
        verify(departmentRepository, never()).save(any(DepartmentEntity.class));
    }

    @Test
    @DisplayName("Cập nhật phòng ban thành công")
    void updateDepartmentSuccessfully() {
        // Given
        UUID departmentId = UUID.randomUUID();
        DepartmentRequestDto requestDto = DepartmentRequestDto.builder()
                .name("Updated IT Department")
                .description("Updated description")
                .build();

        DepartmentEntity existingDepartment = DepartmentEntity.builder()
                .id(departmentId)
                .name("IT Department")
                .description("Old description")
                .build();

        DepartmentEntity updatedDepartment = DepartmentEntity.builder()
                .id(departmentId)
                .name("Updated IT Department")
                .description("Updated description")
                .build();

        DepartmentResponseDto expectedResponse = DepartmentResponseDto.builder()
                .id(departmentId)
                .name("Updated IT Department")
                .description("Updated description")
                .build();

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.existsByName("Updated IT Department")).thenReturn(false);
        when(departmentRepository.save(any(DepartmentEntity.class))).thenReturn(updatedDepartment);
        when(departmentMapper.entityToDto(updatedDepartment)).thenReturn(expectedResponse);

        // When
        DepartmentResponseDto result = departmentService.update(departmentId, requestDto);

        // Then
        assertNotNull(result);
        assertEquals("Updated IT Department", result.getName());
        assertEquals("Updated description", result.getDescription());

        verify(departmentRepository).findById(departmentId);
        verify(departmentRepository).existsByName("Updated IT Department");
        verify(departmentMapper).updateEntityFromDto(requestDto, existingDepartment);
        verify(departmentRepository).save(existingDepartment);
    }

    @Test
    @DisplayName("Xóa phòng ban thành công")
    void deleteDepartmentSuccessfully() {
        // Given
        UUID departmentId = UUID.randomUUID();

        when(departmentRepository.existsById(departmentId)).thenReturn(true);
        when(employmentDetailRepository.countByDepartmentId(departmentId)).thenReturn(0L);

        // When
        assertDoesNotThrow(() -> departmentService.delete(departmentId));

        // Then
        verify(departmentRepository).existsById(departmentId);
        verify(employmentDetailRepository).countByDepartmentId(departmentId);
        verify(conversationService).deleteGroupConversation(departmentId);
        verify(departmentRepository).deleteById(departmentId);
    }

    @Test
    @DisplayName("Lỗi khi xóa phòng ban không tồn tại")
    void failToDeleteNonExistentDepartment() {
        // Given
        UUID departmentId = UUID.randomUUID();

        when(departmentRepository.existsById(departmentId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> departmentService.delete(departmentId)
        );

        assertEquals("Không tìm thấy phòng ban với ID: " + departmentId, exception.getMessage());
        verify(departmentRepository).existsById(departmentId);
        verify(departmentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Lỗi khi xóa phòng ban còn nhân viên")
    void failToDeleteDepartmentWithEmployees() {
        // Given
        UUID departmentId = UUID.randomUUID();

        when(departmentRepository.existsById(departmentId)).thenReturn(true);
        when(employmentDetailRepository.countByDepartmentId(departmentId)).thenReturn(5L);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> departmentService.delete(departmentId)
        );

        assertEquals("Không thể xóa phòng ban vì vẫn còn 5 nhân viên thuộc phòng ban này",
                exception.getMessage());
        verify(departmentRepository).existsById(departmentId);
        verify(employmentDetailRepository).countByDepartmentId(departmentId);
        verify(departmentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Cập nhật phòng ban với thay đổi tên phải cập nhật group chat")
    void updateDepartmentWithNameChangeShouldUpdateGroupChat() {
        // Given
        UUID departmentId = UUID.randomUUID();
        UUID groupChatId = UUID.randomUUID();

        DepartmentRequestDto requestDto = DepartmentRequestDto.builder()
                .name("New IT Department")
                .description("Updated description")
                .build();

        DepartmentEntity existingDepartment = DepartmentEntity.builder()
                .id(departmentId)
                .name("Old IT Department")
                .description("Old description")
                .groupChatId(groupChatId)
                .build();

        DepartmentEntity updatedDepartment = DepartmentEntity.builder()
                .id(departmentId)
                .name("New IT Department")
                .description("Updated description")
                .groupChatId(groupChatId)
                .build();

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.existsByName("New IT Department")).thenReturn(false);
        when(departmentRepository.save(any(DepartmentEntity.class))).thenReturn(updatedDepartment);
        when(departmentMapper.entityToDto(updatedDepartment)).thenReturn(DepartmentResponseDto.builder().build());

        // When
        departmentService.update(departmentId, requestDto);

        // Then
        verify(conversationService).updateConversationName(groupChatId, "New IT Department");
        verify(departmentRepository).save(existingDepartment);
    }

    @Test
    @DisplayName("Cập nhật phòng ban không thay đổi tên không gọi update group chat")
    void updateDepartmentWithoutNameChangeShouldNotUpdateGroupChat() {
        // Given
        UUID departmentId = UUID.randomUUID();
        UUID groupChatId = UUID.randomUUID();

        DepartmentRequestDto requestDto = DepartmentRequestDto.builder()
                .name("IT Department")
                .description("Updated description")
                .build();

        DepartmentEntity existingDepartment = DepartmentEntity.builder()
                .id(departmentId)
                .name("IT Department")
                .description("Old description")
                .groupChatId(groupChatId)
                .build();

        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(existingDepartment));
        when(departmentRepository.save(any(DepartmentEntity.class))).thenReturn(existingDepartment);
        when(departmentMapper.entityToDto(existingDepartment)).thenReturn(DepartmentResponseDto.builder().build());

        // When
        departmentService.update(departmentId, requestDto);

        // Then
        verify(conversationService, never()).updateConversationName(any(), any());
    }
}