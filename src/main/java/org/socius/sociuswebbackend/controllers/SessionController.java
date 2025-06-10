package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.SessionValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    final private OnlineUserService onlineUserService;
    final private SessionValidationService sessionValidationService;

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
