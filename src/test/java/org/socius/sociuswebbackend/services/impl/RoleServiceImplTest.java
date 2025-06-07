package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.events.RBACEvent;
import org.socius.sociuswebbackend.mappers.PermissionMapper;
import org.socius.sociuswebbackend.mappers.RoleMapper;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.model.entities.PermissionEntity;
import org.socius.sociuswebbackend.model.entities.RoleEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionEntity;
import org.socius.sociuswebbackend.model.entities.RolePermissionId;
import org.socius.sociuswebbackend.repositories.EmploymentDetailRepository;
import org.socius.sociuswebbackend.repositories.PermissionRepository;
import org.socius.sociuswebbackend.repositories.RoleRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private EmploymentDetailRepository employmentDetailRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RoleServiceImpl roleService;

    private RoleEntity testRole;
    private RoleRequestDto testRoleRequestDto;
    private RoleResponseDto testRoleResponseDto;
    private PermissionEntity testPermission;
    private PermissionResponseDto testPermissionResponseDto;

    @BeforeEach
    void setUp() {
        // Test Role Entity
        testRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .name("TEST_ROLE")
                .description("Test Role Description")
                .rolePermissions(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Test Role Request DTO
        testRoleRequestDto = RoleRequestDto.builder()
                .name("TEST_ROLE")
                .description("Test Role Description")
                .permissionIds(new HashSet<>())
                .build();

        // Test Role Response DTO
        testRoleResponseDto = RoleResponseDto.builder()
                .id(testRole.getId())
                .name("TEST_ROLE")
                .description("Test Role Description")
                .permissions(new HashSet<>())
                .createdAt(testRole.getCreatedAt())
                .updatedAt(testRole.getUpdatedAt())
                .build();

        // Test Permission Entity
        testPermission = PermissionEntity.builder()
                .id(UUID.randomUUID())
                .name("TEST_PERMISSION")
                .description("Test Permission Description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Test Permission Response DTO
        testPermissionResponseDto = PermissionResponseDto.builder()
                .id(testPermission.getId())
                .name("TEST_PERMISSION")
                .description("Test Permission Description")
                .createdAt(testPermission.getCreatedAt())
                .updatedAt(testPermission.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("Tìm tất cả roles thành công")
    void findAllShouldReturnAllRoles() {
        // Given
        List<RoleEntity> roles = Collections.singletonList(testRole);
        when(roleRepository.findAll()).thenReturn(roles);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        List<RoleResponseDto> result = roleService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRoleResponseDto.getName(), result.getFirst().getName());
        verify(roleRepository).findAll();
        verify(roleMapper).entityToDto(testRole);
    }

    @Test
    @DisplayName("Tìm role theo ID thành công")
    void findByIdShouldReturnRole() {
        // Given
        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        RoleResponseDto result = roleService.findById(testRole.getId());

        // Then
        assertNotNull(result);
        assertEquals(testRoleResponseDto.getName(), result.getName());
        verify(roleRepository).findById(testRole.getId());
        verify(roleMapper).entityToDto(testRole);
    }

    @Test
    @DisplayName("Tìm role theo ID không tồn tại nên throw exception")
    void findByIdShouldThrowExceptionWhenNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(roleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.findById(nonExistentId)
        );

        assertEquals("Không tìm thấy vai trò với ID: " + nonExistentId, exception.getMessage());
        verify(roleRepository).findById(nonExistentId);
        verify(roleMapper, never()).entityToDto(any());
    }

    @Test
    @DisplayName("Tạo role thành công")
    void createShouldReturnCreatedRole() {
        // Given
        when(roleRepository.existsByName(testRoleRequestDto.getName())).thenReturn(false);
        when(roleMapper.requestDtoToEntity(testRoleRequestDto)).thenReturn(testRole);
        when(roleRepository.save(testRole)).thenReturn(testRole);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        RoleResponseDto result = roleService.create(testRoleRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(testRoleResponseDto.getName(), result.getName());
        verify(roleRepository).existsByName(testRoleRequestDto.getName());
        verify(roleRepository).save(testRole);
        verify(roleMapper).requestDtoToEntity(testRoleRequestDto);
        verify(roleMapper).entityToDto(testRole);
    }

    @Test
    @DisplayName("Tạo role với tên đã tồn tại nên throw exception")
    void createShouldThrowExceptionWhenNameExists() {
        // Given
        when(roleRepository.existsByName(testRoleRequestDto.getName())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.create(testRoleRequestDto)
        );

        assertEquals("Vai trò đã tồn tại", exception.getMessage());
        verify(roleRepository).existsByName(testRoleRequestDto.getName());
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo role với lỗi database constraint nên throw IllegalArgumentException")
    void createShouldThrowIllegalArgumentExceptionOnDataIntegrityViolation() {
        // Given
        when(roleRepository.existsByName(testRoleRequestDto.getName())).thenReturn(false);
        when(roleMapper.requestDtoToEntity(testRoleRequestDto)).thenReturn(testRole);
        when(roleRepository.save(testRole)).thenThrow(new DataIntegrityViolationException("Database constraint violation"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.create(testRoleRequestDto)
        );

        assertEquals("Không thể tạo vị trí vì ràng buộc dữ liệu", exception.getMessage());
        verify(roleRepository).save(testRole);
    }

    @Test
    @DisplayName("Cập nhật role thành công")
    void updateShouldReturnUpdatedRole() {
        // Given
        RoleRequestDto updateDto = RoleRequestDto.builder()
                .name("UPDATED_ROLE")
                .description("Updated Description")
                .permissionIds(new HashSet<>())
                .build();

        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName(updateDto.getName())).thenReturn(false);
        when(roleRepository.save(testRole)).thenReturn(testRole);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        RoleResponseDto result = roleService.update(testRole.getId(), updateDto);

        // Then
        assertNotNull(result);
        verify(roleRepository).findById(testRole.getId());
        verify(roleRepository).save(testRole);
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
    }

    @Test
    @DisplayName("Cập nhật role với tên đã tồn tại nên throw exception")
    void updateShouldThrowExceptionWhenNameExists() {
        // Given
        RoleRequestDto updateDto = RoleRequestDto.builder()
                .name("EXISTING_ROLE")
                .description("Updated Description")
                .build();

        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName(updateDto.getName())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.update(testRole.getId(), updateDto)
        );

        assertEquals("Vai trò với tên này đã tồn tại", exception.getMessage());
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xóa role thành công")
    void deleteShouldRemoveRole() {
        // Given
        when(roleRepository.existsById(testRole.getId())).thenReturn(true);
        when(employmentDetailRepository.countByRoleId(testRole.getId())).thenReturn(0L);

        // When
        assertDoesNotThrow(() -> roleService.delete(testRole.getId()));

        // Then
        verify(roleRepository).existsById(testRole.getId());
        verify(employmentDetailRepository).countByRoleId(testRole.getId());
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
        verify(roleRepository).deleteById(testRole.getId());
    }

    @Test
    @DisplayName("Xóa role không tồn tại nên throw exception")
    void deleteShouldThrowExceptionWhenRoleNotFound() {
        // Given
        when(roleRepository.existsById(testRole.getId())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.delete(testRole.getId())
        );

        assertEquals("Không tìm thấy vai trò với ID: " + testRole.getId(), exception.getMessage());
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Xóa role đang được sử dụng nên throw exception")
    void deleteShouldThrowExceptionWhenRoleInUse() {
        // Given
        when(roleRepository.existsById(testRole.getId())).thenReturn(true);
        when(employmentDetailRepository.countByRoleId(testRole.getId())).thenReturn(5L);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.delete(testRole.getId())
        );

        assertEquals("Không thể xóa vai trò vì có nhân viên đang sử dụng vai trò này", exception.getMessage());
        verify(roleRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Lấy tất cả permissions thành công")
    void getAllPermissionsShouldReturnAllPermissions() {
        // Given
        List<PermissionEntity> permissions = Collections.singletonList(testPermission);
        // SỬA: Mock đúng method được gọi trong implementation
        when(roleRepository.findAllPermissions()).thenReturn(permissions);
        when(permissionMapper.entityToDto(testPermission)).thenReturn(testPermissionResponseDto);

        // When
        List<PermissionResponseDto> result = roleService.getAllPermissions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPermissionResponseDto.getName(), result.getFirst().getName());
        // SỬA: Verify đúng method
        verify(roleRepository).findAllPermissions();
        verify(permissionMapper).entityToDto(testPermission);
    }

    @Test
    @DisplayName("Thêm permission vào role thành công")
    void addPermissionToRoleShouldReturnUpdatedRole() {
        // Given
        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(testPermission.getId())).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(testRole)).thenReturn(testRole);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        RoleResponseDto result = roleService.addPermissionToRole(testRole.getId(), testPermission.getId());

        // Then
        assertNotNull(result);
        verify(roleRepository).findById(testRole.getId());
        verify(permissionRepository).findById(testPermission.getId());
        verify(roleRepository).save(testRole);
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
    }

    @Test
    @DisplayName("Thêm permission đã tồn tại vào role nên throw exception")
    void addPermissionToRoleShouldThrowExceptionWhenAlreadyExists() {
        // Given
        RolePermissionEntity existingRolePermission = RolePermissionEntity.builder()
                .role(testRole)
                .permission(testPermission)
                .build();
        testRole.getRolePermissions().add(existingRolePermission);

        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(testPermission.getId())).thenReturn(Optional.of(testPermission));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.addPermissionToRole(testRole.getId(), testPermission.getId())
        );

        assertEquals("Quyền này đã được gán cho vai trò", exception.getMessage());
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thêm nhiều permissions vào role thành công")
    void addPermissionsToRoleShouldReturnUpdatedRole() {
        // Given
        UUID permission2Id = UUID.randomUUID();
        PermissionEntity permission2 = PermissionEntity.builder()
                .id(permission2Id)
                .name("TEST_PERMISSION_2")
                .build();

        List<UUID> permissionIds = Arrays.asList(testPermission.getId(), permission2Id);
        List<PermissionEntity> permissions = Arrays.asList(testPermission, permission2);

        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(permissionRepository.findAllById(permissionIds)).thenReturn(permissions);
        when(roleRepository.save(testRole)).thenReturn(testRole);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        RoleResponseDto result = roleService.addPermissionsToRole(testRole.getId(), permissionIds);

        // Then
        assertNotNull(result);
        verify(roleRepository).findById(testRole.getId());
        verify(permissionRepository).findAllById(permissionIds);
        verify(roleRepository).save(testRole);
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
    }

    @Test
    @DisplayName("Thêm permissions không tồn tại vào role nên throw exception")
    void addPermissionsToRoleShouldThrowExceptionWhenSomePermissionsNotFound() {
        // Given
        List<UUID> permissionIds = Arrays.asList(testPermission.getId(), UUID.randomUUID());
        List<PermissionEntity> permissions = Collections.singletonList(testPermission);

        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(permissionRepository.findAllById(permissionIds)).thenReturn(permissions);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.addPermissionsToRole(testRole.getId(), permissionIds)
        );

        assertEquals("Một số quyền không tồn tại", exception.getMessage());
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xóa permission khỏi role thành công")
    void removePermissionFromRoleShouldUpdateRole() {
        // Given
        RolePermissionEntity rolePermission = RolePermissionEntity.builder()
                .role(testRole)
                .permission(testPermission)
                .build();
        testRole.getRolePermissions().add(rolePermission);

        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        // Bỏ mock permissionRepository.findById nếu implementation không cần

        // When
        assertDoesNotThrow(() -> roleService.removePermissionFromRole(testRole.getId(), testPermission.getId()));

        // Then
        verify(roleRepository).findById(testRole.getId());
        // Bỏ verify permissionRepository.findById
        verify(roleRepository).save(testRole);
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
    }

    @Test
    @DisplayName("Xóa permission không tồn tại trong role nên throw exception")
    void removePermissionFromRoleShouldThrowExceptionWhenPermissionNotInRole() {
        // Given
        when(roleRepository.findById(testRole.getId())).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(testPermission.getId())).thenReturn(Optional.of(testPermission));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.removePermissionFromRole(testRole.getId(), testPermission.getId())
        );

        assertEquals("Quyền không thuộc vai trò này", exception.getMessage());
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xóa nhiều permissions khỏi role thành công")
    void removePermissionsFromRoleShouldUpdateRole() {
        // Given - Tạo dữ liệu test hoàn toàn mới cho test này
        UUID roleId = UUID.randomUUID();
        UUID permission1Id = UUID.randomUUID();
        UUID permission2Id = UUID.randomUUID();

        PermissionEntity permission1 = PermissionEntity.builder()
                .id(permission1Id)
                .name("TEST_PERMISSION_1")
                .build();

        PermissionEntity permission2 = PermissionEntity.builder()
                .id(permission2Id)
                .name("TEST_PERMISSION_2")
                .build();

        RoleEntity roleWithPermissions = RoleEntity.builder()
                .id(roleId)
                .name("TEST_ROLE_WITH_PERMISSIONS")
                .description("Test Role Description")
                .rolePermissions(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        RolePermissionEntity rolePermission1 = RolePermissionEntity.builder()
                .id(RolePermissionId.builder()
                        .roleId(roleId)
                        .permissionId(permission1Id)
                        .build())
                .role(roleWithPermissions)
                .permission(permission1)
                .build();

        RolePermissionEntity rolePermission2 = RolePermissionEntity.builder()
                .id(RolePermissionId.builder()
                        .roleId(roleId)
                        .permissionId(permission2Id)
                        .build())
                .role(roleWithPermissions)
                .permission(permission2)
                .build();

        roleWithPermissions.getRolePermissions().addAll(Arrays.asList(rolePermission1, rolePermission2));
        List<UUID> permissionIds = Arrays.asList(permission1Id, permission2Id);

        // Setup mock với strict behavior
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(roleWithPermissions));
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(roleWithPermissions);

        // When
        assertDoesNotThrow(() -> roleService.removePermissionsFromRole(roleId, permissionIds));

        // Then
        verify(roleRepository).findById(roleId);
        verify(roleRepository).save(any(RoleEntity.class));

        // Verify event được publish với timeout để đảm bảo
        ArgumentCaptor<RBACEvent> eventCaptor = ArgumentCaptor.forClass(RBACEvent.class);
        verify(eventPublisher, timeout(1000)).publishEvent(eventCaptor.capture());

        RBACEvent capturedEvent = eventCaptor.getValue();
        assertEquals(roleId, capturedEvent.getRoleId());
        assertEquals(RBACEvent.EventType.ROLE_UPDATED, capturedEvent.getType());
    }
}