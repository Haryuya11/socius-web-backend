package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.socius.sociuswebbackend.services.RoleService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);
    final private RoleRepository roleRepository;
    final private PermissionRepository permissionRepository;
    final private EmploymentDetailRepository employmentDetailRepository;
    final private RoleMapper roleMapper;
    final private PermissionMapper permissionMapper;
    final private ApplicationEventPublisher eventPublisher;

    @Override
    public List<RoleResponseDto> findAll() {
        List<RoleEntity> roleEntities = roleRepository.findAll();
        return roleEntities.stream()
                .map(roleMapper::entityToDto)
                .toList();
    }

    @Override
    public RoleResponseDto findById(UUID id) {
        return roleRepository.findById(id)
                .map(roleMapper::entityToDto)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + id));
    }

    @Override
    public RoleResponseDto create(RoleRequestDto requestDto) {
        try {
            if (roleRepository.existsByName(requestDto.getName())) {
                throw new RuntimeException("Vai trò đã tồn tại");
            }
            RoleEntity role = roleMapper.requestDtoToEntity(requestDto);
            role = roleRepository.save(role);
            roleRepository.flush();

            if (requestDto.getPermissionIds() != null && !requestDto.getPermissionIds().isEmpty()) {
                Set<RolePermissionEntity> rolePermissions = roleMapper.createRolePermissions(role, requestDto.getPermissionIds());
                role.setRolePermissions(rolePermissions);

                // Save lại để lưu permissions
                role = roleRepository.save(role);
            }

            return roleMapper.entityToDto(role);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Không thể tạo vị trí vì ràng buộc dữ liệu", e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo vị trí: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public RoleResponseDto update(UUID id, RoleRequestDto requestDto) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + id));

        if (!role.getName().equals(requestDto.getName()) &&
                roleRepository.existsByName(requestDto.getName())) {
            throw new IllegalArgumentException("Vai trò với tên này đã tồn tại");
        }

        role.setName(requestDto.getName());
        role.setDescription(requestDto.getDescription());

        // Cập nhật các quyền
        if (requestDto.getPermissionIds() != null && !requestDto.getPermissionIds().isEmpty()) {
            roleMapper.updateRolePermissions(requestDto, role);
        }
        role = roleRepository.save(role);
        // Phát sự kiện cập nhật vai trò
        eventPublisher.publishEvent(new RBACEvent(this, id, RBACEvent.EventType.ROLE_UPDATED));

        return roleMapper.entityToDto(role);
    }

    @Override
    @Transactional
    public void delete(UUID roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Không tìm thấy vai trò với ID: " + roleId);
        }

        long count = employmentDetailRepository.countByRoleId(roleId);

        if (count > 0) {
            throw new RuntimeException("Không thể xóa vai trò vì có nhân viên đang sử dụng vai trò này");
        }

        // Phát sự kiện xóa vai trò
        eventPublisher.publishEvent(new RBACEvent(this, roleId, RBACEvent.EventType.ROLE_DELETED));

        roleRepository.deleteById(roleId);
        logger.info("Đã xóa vai trò với ID: {}", roleId);
    }

    @Override
    public List<PermissionResponseDto> getAllPermissions() {
        List<PermissionEntity> permissionEntities = roleRepository.findAllPermissions();
        return permissionEntities.stream()
                .map(permissionMapper::entityToDto)
                .toList();
    }

    @Override
    @Transactional
    public RoleResponseDto addPermissionToRole(UUID roleId, UUID permissionId) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + roleId));

        PermissionEntity permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền với ID: " + permissionId));

        boolean alreadyExists = role.getRolePermissions().stream()
                .anyMatch(rp -> rp.getPermission().getId().equals(permissionId));

        if (alreadyExists) {
            throw new RuntimeException("Quyền này đã được gán cho vai trò");
        }

        RolePermissionId rolePermissionId = RolePermissionId.builder()
                .roleId(roleId)
                .permissionId(permissionId)
                .build();

        RolePermissionEntity rolePermission = RolePermissionEntity.builder()
                .id(rolePermissionId)
                .role(role)
                .permission(permission)
                .build();

        role.getRolePermissions().add(rolePermission);
        role = roleRepository.save(role);

        // Phát sự kiện cập nhật vai trò
        eventPublisher.publishEvent(new RBACEvent(this, roleId, RBACEvent.EventType.ROLE_UPDATED));

        return roleMapper.entityToDto(role);
    }

    @Override
    public RoleResponseDto addPermissionsToRole(UUID roleId, List<UUID> permissionIds) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + roleId));

        List<PermissionEntity> permissions = permissionRepository.findAllById(permissionIds);
        if (permissions.size() != permissionIds.size()) {
            throw new RuntimeException("Một số quyền không tồn tại");
        }

        Set<UUID> existingPermissionIds = role.getRolePermissions().stream()
                .map(rp -> rp.getPermission().getId())
                .collect(Collectors.toSet());

        Set<RolePermissionEntity> newRolePermissions = new HashSet<>();
        for (PermissionEntity permission : permissions) {
            if (!existingPermissionIds.contains(permission.getId())) {
                RolePermissionId rolePermissionId = RolePermissionId.builder()
                        .roleId(roleId)
                        .permissionId(permission.getId())
                        .build();

                RolePermissionEntity rolePermission = RolePermissionEntity.builder()
                        .id(rolePermissionId)
                        .role(role)
                        .permission(permission)
                        .build();

                newRolePermissions.add(rolePermission);
            }
        }

        role.getRolePermissions().addAll(newRolePermissions);
        role = roleRepository.save(role);

        eventPublisher.publishEvent(new RBACEvent(this, roleId, RBACEvent.EventType.ROLE_UPDATED));

        logger.info("Đã thêm các quyền vào vai trò với ID: {}", roleId);
        return roleMapper.entityToDto(role);
    }

    @Override
    @Transactional
    public void removePermissionFromRole(UUID roleId, UUID permissionId) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + roleId));

        boolean removed = role.getRolePermissions().removeIf(
                rp -> rp.getPermission().getId().equals(permissionId)
        );

        if (!removed) {
            throw new RuntimeException("Quyền không thuộc vai trò này");
        }

        roleRepository.save(role);

        CompletableFuture.runAsync(() -> eventPublisher.publishEvent(new RBACEvent(this, roleId, RBACEvent.EventType.ROLE_UPDATED)));

        logger.info("Đã xóa quyền {} khỏi vai trò {}", permissionId, roleId);
    }

    @Override
    @Transactional
    public void removePermissionsFromRole(UUID roleId, List<UUID> permissionIds) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + roleId));

        Set<UUID> permissionIdsSet = new HashSet<>(permissionIds);
        int removedCount = role.getRolePermissions().size();

        role.getRolePermissions().removeIf(
                rp -> permissionIdsSet.contains(rp.getPermission().getId())
        );

        removedCount = removedCount - role.getRolePermissions().size();
        roleRepository.save(role);

        CompletableFuture.runAsync(() -> eventPublisher.publishEvent(new RBACEvent(this, roleId, RBACEvent.EventType.ROLE_UPDATED)));

        logger.info("Đã xóa {} quyền khỏi vai trò {}", removedCount, roleId);
    }

    @Override
    public List<PermissionResponseDto> getRolePermissions(UUID roleId) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò với ID: " + roleId));

        return role.getRolePermissions().stream()
                .map(rp -> permissionMapper.entityToDto(rp.getPermission()))
                .toList();
    }
}
