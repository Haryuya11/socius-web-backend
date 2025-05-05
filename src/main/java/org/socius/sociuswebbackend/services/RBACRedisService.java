package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;

import java.util.UUID;

public interface RBACRedisService {
    /**
     * Lưu thông tin quyền của người dùng vào Redis
     *
     * @param sessionId         Id phiên
     * @param permissionsDto    Thông tin quyền của người dùng
     * @param expiryTimeMinutes Thời gian hết hạn (phút)
     */
    void saveCacheUserPermissions(String sessionId, UserPermissionsDto permissionsDto, int expiryTimeMinutes);

    /**
     * Lấy thông tin quyền của người dùng từ Redis
     *
     * @param sessionId Id phiên
     * @return Thông tin quyền của người dùng
     */
    UserPermissionsDto getUserPermissions(String sessionId);

    /**
     * Kiểm tra xem người dùng có quyền cụ thể hay không
     *
     * @param sessionId  Id phiên
     * @param permission Quyền cần kiểm tra
     * @return true nếu người dùng có quyền, false nếu không
     */
    boolean hasPermission(String sessionId, String permission);

    /**
     * Kiểm tra xem người dùng có vai trò cụ thể hay không
     *
     * @param sessionId Id phiên
     * @param roleName  Tên vai trò cần kiểm tra
     * @return true nếu người dùng có vai trò, false nếu không
     */
    boolean hasRole(String sessionId, String roleName);

    /**
     * Xóa thông tin quyền của người dùng khỏi Redis
     *
     * @param sessionId Id phiên
     */
    void deleteUserPermissions(String sessionId);

    /**
     * Xóa tất cả thông tin quyền liên quan đến một vai trò
     *
     * @param roleId Id vai trò
     * @return Số lượng bản ghi đã xóa
     */
    long deleteByRoleId(UUID roleId);

    /**
     * Gia hạn thời gian hết hạn của cache
     *
     * @param sessionId         Id phiên
     * @param expiryTimeMinutes Thời gian hết hạn mới (phút)
     * @return true nếu gia hạn thành công, false nếu không
     */
    boolean extendExpiration(String sessionId, int expiryTimeMinutes);
}
