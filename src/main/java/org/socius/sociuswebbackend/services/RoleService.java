package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.permission.PermissionResponseDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleRequestDto;
import org.socius.sociuswebbackend.model.dtos.role.RoleResponseDto;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    /**
     * Lấy danh sách tất cả các vai trò
     *
     * @return Danh sách các vai trò
     */
    List<RoleResponseDto> findAll();

    /**
     * Tìm một vai trò theo ID
     *
     * @param id ID của vai trò cần tìm
     * @return vai trò nếu tìm thấy, null nếu không tìm thấy
     */
    RoleResponseDto findById(UUID id);

    /**
     * Tạo một vai trò mới
     *
     * @param requestDto Thông tin yêu cầu tạo vai trò
     * @return Thông tin vai trò đã được tạo
     */
    RoleResponseDto create(RoleRequestDto requestDto);

    /**
     * Cập nhật thông tin một vai trò
     *
     * @param id         ID của vai trò cần cập nhật
     * @param requestDto Thông tin yêu cầu cập nhật vai trò
     * @return Thông tin vai trò đã được cập nhật
     */
    RoleResponseDto update(UUID id, RoleRequestDto requestDto);

    /**
     * Xóa một vai trò
     *
     * @param roleId ID của vai trò cần xóa
     */
    void delete(UUID roleId);

    /**
     * Lấy danh sách tất cả các quyền của hệ thống
     *
     * @return Danh sách các quyền
     */
    List<PermissionResponseDto> getAllPermissions();

    /**
     * Thêm một permission vào role
     *
     * @param roleId ID của vai trò cần thêm permission
     * @param permissionId ID của permission cần thêm
     * @return Thông tin vai trò đã được cập nhật
     */
    RoleResponseDto addPermissionToRole(UUID roleId, UUID permissionId);

    /**
     * Thêm nhiều permission vào role
     *
     * @param roleId ID của vai trò
     * @param permissionIds Danh sách ID của các permission cần thêm
     * @return Thông tin vai trò đã được cập nhật
     */
    RoleResponseDto addPermissionsToRole(UUID roleId, List<UUID> permissionIds);

    /**
     * Xóa permission khỏi role
     *
     * @param roleId ID của vai trò
     * @param permissionId ID của permission cần xóa
     */
    void removePermissionFromRole(UUID roleId, UUID permissionId);

    /**
     * Xóa nhiều permission khỏi role
     *
     * @param roleId ID của vai trò
     * @param permissionIds Danh sách ID của các permission cần xóa
     */
    void removePermissionsFromRole(UUID roleId, List<UUID> permissionIds);

    /**
     * Lấy danh sách permission của một role
     *
     * @param roleId ID của vai trò
     * @return Danh sách permission của role
     */
    List<PermissionResponseDto> getRolePermissions(UUID roleId);


}