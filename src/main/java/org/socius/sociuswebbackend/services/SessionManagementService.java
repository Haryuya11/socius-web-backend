package org.socius.sociuswebbackend.services;

import java.util.Set;
import java.util.UUID;

public interface SessionManagementService {

    /**
     * Lấy danh sách các phiên (session) của một người dùng có role cụ thể
     *
     * @param roleId ID của role
     * @return Danh sách các session của người dùng
     */
    Set<String> getSessionsByRoleId(UUID roleId);

    /**
     * Hủy phiên làm việc theo sessionId
     * @param sessionId ID của phiên làm việc cần hủy
     * @return true nếu hủy thành công, false nếu không tìm thấy phiên làm việc
     */
    boolean invalidateSession(String sessionId);


}