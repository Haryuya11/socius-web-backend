package org.socius.sociuswebbackend.controllers;

import java.util.List;

import org.socius.sociuswebbackend.model.dtos.user.OnlineUserDto;
import org.socius.sociuswebbackend.services.SessionManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private SessionManagementService sessionManagementService;

    /**
     * Lấy danh sách người dùng đang hoạt động trong hệ thống
     * Endpoint này yêu cầu quyền "CAN" để truy cập (được cấu hình trong SecurityConfig)
     * 
     * @return Danh sách người dùng đang online
     */
    @GetMapping("/active-users")
    public ResponseEntity<List<OnlineUserDto>> getOnlineUsers() {
        List<OnlineUserDto> activeUsers = sessionManagementService.getOnlineUsers();
        return ResponseEntity.ok(activeUsers);
    }

    /**
     * Kiểm tra trạng thái hoạt động của một người dùng cụ thể
     * 
     * @param userId ID của người dùng cần kiểm tra
     * @return true nếu người dùng đang online, false nếu không
     */
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<Boolean> checkUserStatus(@PathVariable String userId) {
        boolean isActive = sessionManagementService.isUserActive(userId);
        return ResponseEntity.ok(isActive);
    }
}
