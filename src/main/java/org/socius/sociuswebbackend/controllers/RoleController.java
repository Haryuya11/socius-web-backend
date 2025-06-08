package org.socius.sociuswebbackend.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;
import org.socius.sociuswebbackend.services.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ACCESS_ADMIN_PAGE')")
public class RoleController {

    final private RoleService roleService;

    /**
     * Lấy danh sách tất cả các vai trò
     *
     * @return Danh sách các vai trò
     */
    @GetMapping("/all")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        List<RoleResponseDto> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    /**
     * Lấy thông tin một vai trò theo ID
     *
     * @param id ID của vai trò cần tìm
     * @return Thông tin vai trò nếu tìm thấy, null nếu không tìm thấy
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> getRoleById(@PathVariable UUID id) {
        RoleResponseDto role = roleService.findById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Tạo một vai trò mới
     *
     * @param requestDto Thông tin yêu cầu tạo vai trò
     * @return Thông tin vai trò đã được tạo
     */
    @PostMapping("/create")
    public ResponseEntity<RoleResponseDto> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto createdRole = roleService.create(requestDto);
        return ResponseEntity.ok(createdRole);
    }

    /**
     * Cập nhật thông tin một vai trò
     *
     * @param id         ID của vai trò cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật vai trò
     * @return Thông tin vai trò đã được cập nhật
     */
    @PostMapping("/update/{id}")
    public ResponseEntity<RoleResponseDto> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequestDto requestDto) {
        RoleResponseDto updatedRole = roleService.update(id, requestDto);
        return ResponseEntity.ok(updatedRole);
    }

    /**
     * Xóa một vai trò theo ID
     *
     * @param id ID của vai trò cần xóa
     * @return ResponseEntity với status 200 nếu thành công, 404 nếu không tìm thấy vai trò
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy danh sách tất cả các permission
     *
     * @return Danh sách các permission
     */
    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponseDto>> getAllPermissions() {
        List<PermissionResponseDto> permissions = roleService.getAllPermissions();
        if (permissions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(permissions);
    }

    /**
     * Thêm một permission vào role
     *
     * @param roleId       ID của vai trò cần thêm permission
     * @param permissionId ID của permission cần thêm
     * @return Thông tin vai trò đã được cập nhật
     */
    @PostMapping("/add/{roleId}/permissions/{permissionId}")
    public ResponseEntity<RoleResponseDto> addPermissionToRole(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {
        RoleResponseDto updatedRole = roleService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok(updatedRole);
    }

    /**
     * Thêm nhiều permission vào role
     *
     * @param roleId        ID của vai trò
     * @param permissionIds Danh sách ID của các permission cần thêm
     * @return Thông tin vai trò đã được cập nhật
     */
    @PostMapping("/add/{roleId}/permissions")
    public ResponseEntity<RoleResponseDto> addPermissionsToRole(
            @PathVariable UUID roleId,
            @RequestBody List<UUID> permissionIds) {
        RoleResponseDto result = roleService.addPermissionsToRole(roleId, permissionIds);
        return ResponseEntity.ok(result);
    }

    /**
     * Xóa một permission khỏi role
     *
     * @param roleId       ID của vai trò
     * @param permissionId ID của permission cần xóa khỏi role
     * @return ResponseEntity với status 204 No Content
     */
    @DeleteMapping("/remove/{roleId}/permissions/{permissionId}")
    public ResponseEntity<?> removePermissionFromRole(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Xóa nhiều permission khỏi role
     *
     * @param roleId        ID của vai trò
     * @param permissionIds Danh sách ID của các permission cần xóa
     * @return ResponseEntity với status 204 No Content
     */
    @DeleteMapping("/remove/{roleId}/permissions")
    public ResponseEntity<?> removePermissionsFromRole(
            @PathVariable UUID roleId,
            @RequestBody List<UUID> permissionIds) {
        roleService.removePermissionsFromRole(roleId, permissionIds);
        return ResponseEntity.noContent().build();
    }
}
