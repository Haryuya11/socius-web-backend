package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;

import java.util.List;
import java.util.UUID;

public interface OnlineUserService {
    /**
     * Cập nhật trạng thái online của người dùng
     *
     * @param userId ID của người dùng
     * @param sessionId ID phiên của người dùng
     */
    void updateUserOnlineStatus(UUID userId, String sessionId);

    /**
     * Xử lý heartbeat từ người dùng
     *
     * @param userId ID của người dùng
     */
    void handleUserHeartbeat(UUID userId);

    /**
     * Đánh dấu người dùng offline
     *
     * @param userId ID của người dùng
     * @param sessionId ID phiên của người dùng
     */
    void markUserOffline(UUID userId, String sessionId);

    /**
     * Lấy danh sách người dùng đang online
     *
     * @return Danh sách người dùng online
     */
    List<OnlineUserStatusDto> getOnlineUsers();

    /**
     * Kiểm tra trạng thái online của người dùng
     *
     * @param userId ID của người dùng cần kiểm tra
     * @return true nếu người dùng đang online, false nếu không
     */
    boolean isUserOnline(UUID userId);



    /**
     * Lấy ID phiên của người dùng
     *
     * @param userId ID của người dùng
     * @return ID phiên của người dùng, hoặc null nếu không tìm thấy
     */
    String getUserSessionId(UUID userId);

    /**
     * Kiểm tra session của người dùng có hợp lệ không
     *
     * @param userId ID của người dùng
     * @return true nếu session hợp lệ, false nếu không
     */
    boolean isUserSessionValid(UUID userId);

}