package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.SessionManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    final private OnlineUserService onlineUserService;

    /**
     * Lấy danh sách người dùng đang online
     *
     * @return Danh sách người dùng online
     */
    @GetMapping("/online-users")
    public ResponseEntity<List<OnlineUserStatusDto>> getOnlineUsers() {
        List<OnlineUserStatusDto> onlineUsers = onlineUserService.getOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }

    /**
     * Kiểm tra trạng thái hoạt động của một người dùng cụ thể
     *
     * @param userId ID của người dùng cần kiểm tra
     * @return true nếu người dùng đang online, false nếu không
     */
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<Boolean> checkUserStatus(@PathVariable UUID userId) {
        boolean isOnline = onlineUserService.isUserOnline(userId);
        return ResponseEntity.ok(isOnline);
    }
}
