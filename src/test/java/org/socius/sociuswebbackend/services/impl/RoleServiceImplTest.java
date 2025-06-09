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
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceImplTest {

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
    private UUID testRoleId;
    private UUID testPermissionId;

    @BeforeEach
    void setUp() {
        testRoleId = UUID.randomUUID();
        testPermissionId = UUID.randomUUID();

        testRole = RoleEntity.builder()
                .id(testRoleId)
                .name("ADMIN")
                .description("Administrator role")
                .rolePermissions(new HashSet<>())
                .build();
        testRole.setCreatedAt(LocalDateTime.now());
        testRole.setUpdatedAt(LocalDateTime.now());

        testRoleRequestDto = RoleRequestDto.builder()
                .name("ADMIN")
                .description("Administrator role")
                .permissionIds(new HashSet<>(List.of(testPermissionId)))
                .build();

        testRoleResponseDto = RoleResponseDto.builder()
                .id(testRoleId)
                .name("ADMIN")
                .description("Administrator role")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .permissions(new HashSet<>())
                .build();

        testPermission = PermissionEntity.builder()
                .id(testPermissionId)
                .name("ACCESS_ADMIN_PAGE")
                .description("Access to admin page")
                .build();

        testPermissionResponseDto = PermissionResponseDto.builder()
                .id(testPermissionId)
                .name("ACCESS_ADMIN_PAGE")
                .description("Access to admin page")
                .build();
    }

    @Test
    @DisplayName("Lấy danh sách tất cả roles thành công")
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
    @DisplayName("Lấy danh sách roles đang hoạt động thành công")
    void findAllActiveRolesShouldReturnActiveRoles() {
        // Given
        List<RoleEntity> activeRoles = Collections.singletonList(testRole);
        when(roleRepository.findAllActiveRoles()).thenReturn(activeRoles);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        List<RoleResponseDto> result = roleService.findAllActiveRoles();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(roleRepository).findAllActiveRoles();
        verify(roleMapper).entityToDto(testRole);
    }

    @Test
    @DisplayName("Tìm role theo ID thành công")
    void findByIdShouldReturnRole() {
        // Given
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        RoleResponseDto result = roleService.findById(testRoleId);

        // Then
        assertNotNull(result);
        assertEquals(testRoleResponseDto.getName(), result.getName());
        verify(roleRepository).findById(testRoleId);
        verify(roleMapper).entityToDto(testRole);
    }

    @Test
    @DisplayName("Tìm role theo ID không tồn tại sẽ ném exception")
    void findByIdWithNonExistentIdShouldThrowException() {
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
    }

    @Test
    @DisplayName("Tạo role thành công")
    void createRoleShouldSucceed() {
        // Given
        when(roleRepository.existsByName(testRoleRequestDto.getName())).thenReturn(false);

        RoleEntity savedRole = RoleEntity.builder()
                .id(testRoleId)
                .name(testRoleRequestDto.getName())
                .description(testRoleRequestDto.getDescription())
                .rolePermissions(new HashSet<>())
                .build();
        savedRole.setCreatedAt(LocalDateTime.now());
        savedRole.setUpdatedAt(LocalDateTime.now());

        when(roleRepository.saveAndFlush(any(RoleEntity.class))).thenReturn(savedRole);
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(savedRole);
        when(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission));

        // When
        RoleResponseDto result = roleService.create(testRoleRequestDto);

        // Then
        assertNotNull(result);
        assertEquals(testRoleRequestDto.getName(), result.getName());
        assertEquals(testRoleRequestDto.getDescription(), result.getDescription());
        verify(roleRepository).existsByName(testRoleRequestDto.getName());
        verify(roleRepository).saveAndFlush(any(RoleEntity.class));
        verify(permissionRepository).findById(testPermissionId);
    }

    @Test
    @DisplayName("Tạo role với tên đã tồn tại sẽ ném exception")
    void createRoleWithExistingNameShouldThrowException() {
        // Given
        when(roleRepository.existsByName(testRoleRequestDto.getName())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.create(testRoleRequestDto)
        );

        assertEquals("Vai trò đã tồn tại", exception.getMessage());
        verify(roleRepository).existsByName(testRoleRequestDto.getName());
        verify(roleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Tạo role với DataIntegrityViolationException sẽ ném IllegalArgumentException")
    void createRoleWithDataIntegrityViolationShouldThrowIllegalArgumentException() {
        // Given
        when(roleRepository.existsByName(testRoleRequestDto.getName())).thenReturn(false);
        when(roleRepository.saveAndFlush(any(RoleEntity.class))).thenThrow(new DataIntegrityViolationException("DB error"));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.create(testRoleRequestDto)
        );

        assertTrue(exception.getMessage().contains("Không thể tạo vai trò vì ràng buộc dữ liệu"));
        verify(roleRepository).existsByName(testRoleRequestDto.getName());
        verify(roleRepository).saveAndFlush(any(RoleEntity.class));
    }

    @Test
    @DisplayName("Cập nhật role thành công")
    void updateRoleShouldSucceed() {
        // Given
        RoleRequestDto updateRequest = RoleRequestDto.builder()
                .name("UPDATED_ADMIN")
                .description("Updated description")
                .permissionIds(new HashSet<>())
                .build();

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName(updateRequest.getName())).thenReturn(false);
        when(roleRepository.save(testRole)).thenReturn(testRole);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        RoleResponseDto result = roleService.update(testRoleId, updateRequest);

        // Then
        assertNotNull(result);
        verify(roleRepository).findById(testRoleId);
        verify(roleRepository).existsByName(updateRequest.getName());
        verify(roleRepository).save(testRole);
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
        verify(roleMapper).entityToDto(testRole);
    }

    @Test
    @DisplayName("Cập nhật role không tồn tại sẽ ném exception")
    void updateNonExistentRoleShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(roleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.update(nonExistentId, testRoleRequestDto)
        );

        assertEquals("Không tìm thấy vai trò với ID: " + nonExistentId, exception.getMessage());
        verify(roleRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Cập nhật role với tên đã tồn tại sẽ ném exception")
    void updateRoleWithExistingNameShouldThrowException() {
        // Given
        RoleRequestDto updateRequest = RoleRequestDto.builder()
                .name("EXISTING_ROLE")
                .description("Description")
                .build();

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(roleRepository.existsByName(updateRequest.getName())).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> roleService.update(testRoleId, updateRequest)
        );

        assertEquals("Vai trò với tên này đã tồn tại", exception.getMessage());
        verify(roleRepository).findById(testRoleId);
        verify(roleRepository).existsByName(updateRequest.getName());
    }

    @Test
    @DisplayName("Xóa role thành công")
    void deleteRoleShouldSucceed() {
        // Given
        when(roleRepository.existsById(testRoleId)).thenReturn(true);
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(employmentDetailRepository.countByRoleId(testRoleId)).thenReturn(0L);

        // When
        assertDoesNotThrow(() -> roleService.delete(testRoleId));

        // Then
        verify(roleRepository).existsById(testRoleId);
        verify(roleRepository).findById(testRoleId);
        verify(employmentDetailRepository).countByRoleId(testRoleId);
        verify(roleRepository).save(testRole);
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
    }

    @Test
    @DisplayName("Xóa role không tồn tại sẽ ném exception")
    void deleteNonExistentRoleShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(roleRepository.existsById(nonExistentId)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.delete(nonExistentId)
        );

        assertEquals("Không tìm thấy vai trò với ID: " + nonExistentId, exception.getMessage());
        verify(roleRepository).existsById(nonExistentId);
    }

    @Test
    @DisplayName("Xóa role đang được sử dụng sẽ ném exception")
    void deleteRoleInUseShouldThrowException() {
        // Given
        when(roleRepository.existsById(testRoleId)).thenReturn(true);
        when(employmentDetailRepository.countByRoleId(testRoleId)).thenReturn(1L);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.delete(testRoleId)
        );

        assertEquals("Không thể xóa vai trò vì có nhân viên đang sử dụng vai trò này", exception.getMessage());
        verify(roleRepository).existsById(testRoleId);
        verify(employmentDetailRepository).countByRoleId(testRoleId);
    }

    @Test
    @DisplayName("Lấy tất cả permissions thành công")
    void getAllPermissionsShouldReturnAllPermissions() {
        // Given
        List<PermissionEntity> permissions = Collections.singletonList(testPermission);
        when(roleRepository.findAllPermissions()).thenReturn(permissions);
        when(permissionMapper.entityToDto(testPermission)).thenReturn(testPermissionResponseDto);

        // When
        List<PermissionResponseDto> result = roleService.getAllPermissions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPermissionResponseDto.getName(), result.getFirst().getName());
        verify(roleRepository).findAllPermissions();
        verify(permissionMapper).entityToDto(testPermission);
    }

    @Test
    @DisplayName("Thêm permission vào role thành công")
    void addPermissionToRoleShouldSucceed() {
        // Given
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(testRole)).thenReturn(testRole);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        RoleResponseDto result = roleService.addPermissionToRole(testRoleId, testPermissionId);

        // Then
        assertNotNull(result);
        verify(roleRepository).findById(testRoleId);
        verify(permissionRepository).findById(testPermissionId);
        verify(roleRepository).save(testRole);
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
        verify(roleMapper).entityToDto(testRole);
    }

    @Test
    @DisplayName("Thêm permission vào role không tồn tại sẽ ném exception")
    void addPermissionToNonExistentRoleShouldThrowException() {
        // Given
        UUID nonExistentRoleId = UUID.randomUUID();
        when(roleRepository.findById(nonExistentRoleId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.addPermissionToRole(nonExistentRoleId, testPermissionId)
        );

        assertEquals("Không tìm thấy vai trò với ID: " + nonExistentRoleId, exception.getMessage());
        verify(roleRepository).findById(nonExistentRoleId);
    }

    @Test
    @DisplayName("Thêm permission không tồn tại vào role sẽ ném exception")
    void addNonExistentPermissionToRoleShouldThrowException() {
        // Given
        UUID nonExistentPermissionId = UUID.randomUUID();
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(nonExistentPermissionId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.addPermissionToRole(testRoleId, nonExistentPermissionId)
        );

        assertEquals("Không tìm thấy quyền với ID: " + nonExistentPermissionId, exception.getMessage());
        verify(roleRepository).findById(testRoleId);
        verify(permissionRepository).findById(nonExistentPermissionId);
    }

    @Test
    @DisplayName("Thêm permission đã tồn tại vào role sẽ ném exception")
    void addExistingPermissionToRoleShouldThrowException() {
        // Given
        RolePermissionEntity existingRolePermission = RolePermissionEntity.builder()
                .id(RolePermissionId.builder()
                        .roleId(testRoleId)
                        .permissionId(testPermissionId)
                        .build())
                .role(testRole)
                .permission(testPermission)
                .build();

        testRole.getRolePermissions().add(existingRolePermission);

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findById(testPermissionId)).thenReturn(Optional.of(testPermission));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.addPermissionToRole(testRoleId, testPermissionId)
        );

        assertEquals("Quyền này đã được gán cho vai trò", exception.getMessage());
        verify(roleRepository).findById(testRoleId);
        verify(permissionRepository).findById(testPermissionId);
    }

    @Test
    @DisplayName("Thêm nhiều permissions vào role thành công")
    void addPermissionsToRoleShouldSucceed() {
        // Given
        List<UUID> permissionIds = Collections.singletonList(testPermissionId);
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findAllById(permissionIds)).thenReturn(Collections.singletonList(testPermission));
        when(roleRepository.save(testRole)).thenReturn(testRole);
        when(roleMapper.entityToDto(testRole)).thenReturn(testRoleResponseDto);

        // When
        RoleResponseDto result = roleService.addPermissionsToRole(testRoleId, permissionIds);

        // Then
        assertNotNull(result);
        verify(roleRepository).findById(testRoleId);
        verify(permissionRepository).findAllById(permissionIds);
        verify(roleRepository).save(testRole);
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
        verify(roleMapper).entityToDto(testRole);
    }

    @Test
    @DisplayName("Thêm permissions không đầy đủ vào role sẽ ném exception")
    void addIncompletePermissionsToRoleShouldThrowException() {
        // Given
        List<UUID> permissionIds = Arrays.asList(testPermissionId, UUID.randomUUID());
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findAllById(permissionIds)).thenReturn(Collections.singletonList(testPermission));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.addPermissionsToRole(testRoleId, permissionIds)
        );

        assertEquals("Một số quyền không tồn tại", exception.getMessage());
        verify(roleRepository).findById(testRoleId);
        verify(permissionRepository).findAllById(permissionIds);
    }

    @Test
    @DisplayName("Xóa permission khỏi role thành công")
    void removePermissionFromRoleShouldSucceed() {
        // Given
        RolePermissionEntity rolePermission = RolePermissionEntity.builder()
                .id(RolePermissionId.builder()
                        .roleId(testRoleId)
                        .permissionId(testPermissionId)
                        .build())
                .role(testRole)
                .permission(testPermission)
                .build();

        testRole.getRolePermissions().add(rolePermission);

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));

        // When
        assertDoesNotThrow(() -> roleService.removePermissionFromRole(testRoleId, testPermissionId));

        // Then
        verify(roleRepository).findById(testRoleId);
        verify(roleRepository).save(testRole);
        verify(eventPublisher).publishEvent(any(RBACEvent.class));
    }

    @Test
    @DisplayName("Xóa permission không thuộc role sẽ ném exception")
    void removePermissionNotInRoleShouldThrowException() {
        // Given
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.removePermissionFromRole(testRoleId, testPermissionId)
        );

        assertEquals("Quyền không thuộc vai trò này", exception.getMessage());
        verify(roleRepository).findById(testRoleId);
    }

    @Test
    @DisplayName("Should remove permissions from role successfully")
    void removePermissionsFromRoleShouldSucceed() {
        // Given
        UUID roleId = UUID.randomUUID();
        List<UUID> permissionIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());

        RoleEntity roleEntity = getRoleEntity(roleId, permissionIds);

        when(roleRepository.findById(roleId)).thenReturn(Optional.of(roleEntity));
        when(roleRepository.save(any(RoleEntity.class))).thenReturn(roleEntity);

        // When
        roleService.removePermissionsFromRole(roleId, permissionIds);

        // Then - Chờ async operation hoàn thành
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> verify(eventPublisher).publishEvent(any(RBACEvent.class)));

        verify(roleRepository).findById(roleId);
        verify(roleRepository).save(roleEntity);
        assertEquals(0, roleEntity.getRolePermissions().size());
    }

    private static RoleEntity getRoleEntity(UUID roleId, List<UUID> permissionIds) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(roleId);

        // Setup role permissions
        Set<RolePermissionEntity> rolePermissions = new HashSet<>();
        for (UUID permissionId : permissionIds) {
            PermissionEntity permission = new PermissionEntity();
            permission.setId(permissionId);

            RolePermissionEntity rp = new RolePermissionEntity();
            rp.setPermission(permission);
            rolePermissions.add(rp);
        }
        roleEntity.setRolePermissions(rolePermissions);
        return roleEntity;
    }

    @Test
    @DisplayName("Lấy permissions của role thành công")
    void getRolePermissionsShouldReturnPermissions() {
        // Given
        RolePermissionEntity rolePermission = RolePermissionEntity.builder()
                .id(RolePermissionId.builder()
                        .roleId(testRoleId)
                        .permissionId(testPermissionId)
                        .build())
                .role(testRole)
                .permission(testPermission)
                .build();

        testRole.getRolePermissions().add(rolePermission);

        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(permissionMapper.entityToDto(testPermission)).thenReturn(testPermissionResponseDto);

        // When
        List<PermissionResponseDto> result = roleService.getRolePermissions(testRoleId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPermissionResponseDto.getName(), result.getFirst().getName());
        verify(roleRepository).findById(testRoleId);
        verify(permissionMapper).entityToDto(testPermission);
    }

    @Test
    @DisplayName("Lấy permissions của role không tồn tại sẽ ném exception")
    void getRolePermissionsWithNonExistentRoleShouldThrowException() {
        // Given
        UUID nonExistentRoleId = UUID.randomUUID();
        when(roleRepository.findById(nonExistentRoleId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.getRolePermissions(nonExistentRoleId)
        );

        assertEquals("Không tìm thấy vai trò với ID: " + nonExistentRoleId, exception.getMessage());
        verify(roleRepository).findById(nonExistentRoleId);
    }
}