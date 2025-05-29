package org.socius.sociuswebbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.SessionValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);
    final private OnlineUserService onlineUserService;
    final private SessionValidationService sessionValidationService;
    final private ConfigService configService;


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


    /**
     * Kiểm tra session của người dùng có hợp lệ không (dựa trên RBAC key trong Redis)
     *
     * @param userId ID của người dùng cần kiểm tra
     * @return true nếu session hợp lệ, false nếu không
     */
    @GetMapping("/user/{userId}/valid")
    public ResponseEntity<Boolean> checkUserSessionValid(@PathVariable UUID userId) {
        boolean isValid = sessionValidationService.hasValidSession(userId);
        return ResponseEntity.ok(isValid);
    }

    /**
     * Kiểm tra session theo sessionId (dựa trên RBAC key trong Redis)
     */
    @GetMapping("/{sessionId}/valid")
    public ResponseEntity<Boolean> checkSessionValid(@PathVariable String sessionId) {
        boolean isValid = sessionValidationService.isSessionValid(sessionId);
        return ResponseEntity.ok(isValid);
    }

    /**
     * Lấy sessionId của user (nếu có session hợp lệ)
     */
    @GetMapping("/user/{userId}/session-id")
    public ResponseEntity<String> getUserSessionId(@PathVariable UUID userId) {
        String sessionId = sessionValidationService.getUserSessionId(userId);
        if (sessionId != null) {
            return ResponseEntity.ok(sessionId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
