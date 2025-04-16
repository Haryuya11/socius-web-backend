package org.socius.sociuswebbackend.services;

import java.util.List;

import org.socius.sociuswebbackend.model.dtos.user.OnlineUserDto;

public interface SessionManagementService {
    /**
     * Lấy danh sách người dùng đang hoạt động (đang đăng nhập) trong hệ thống
     * 
     * @return Danh sách người dùng đang online
     */
    List<OnlineUserDto> getOnlineUsers();
    
    /**
     * Kiểm tra xem một người dùng có đang hoạt động hay không
     * 
     * @param userId ID của người dùng cần kiểm tra
     * @return true nếu người dùng đang online, false nếu không
     */
    boolean isUserActive(String userId);
}