package org.socius.sociuswebbackend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.TypingIndicatorDto;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.UserService;
import org.socius.sociuswebbackend.services.WebSocketService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user-online")
@RequiredArgsConstructor
public class UserOnlineController {
    private static final Logger logger = LoggerFactory.getLogger(UserOnlineController.class);

    final private OnlineUserService onlineUserService;
    final private WebSocketService webSocketService;
    final private RedisTemplate<String, Object> redisTemplate;
    final private UserService userService;

    /**
     * Xử lý thông điệp từ client để cập nhật trạng thái hoạt động của người dùng
     *
     * @param headerAccessor Thông tin tiêu đề của thông điệp
     */
    @MessageMapping("/heartbeat")
    public void processHeartbeat(SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return;
        }

        String userKey = RedisKeyBuilder.userIdAttributeKey();
        UUID userId = (UUID) sessionAttributes.get(userKey);

        if (userId == null) {
            logger.warn("UserId null trong heartbeat");
            return;
        }
        String sessionId = headerAccessor.getSessionId();
        if (sessionId == null) {
            logger.warn("SessionId null trong heartbeat");
            return;
        }
        String sessionKey = RedisKeyBuilder.springSessionKey(sessionId);
        if (!redisTemplate.hasKey(sessionKey)) {
            onlineUserService.markUserOffline(userId, sessionId);
            webSocketService.sendSessionInvalidationNotification(
                    sessionId,
                    "SESSION_EXPIRED",
                    "Phiên làm việc đã hết hạn"
            );
            return;
        }
        onlineUserService.handleUserHeartbeat(userId);
        logger.debug("Heartbeat nhận được từ userId: {}, sessionId: {}", userId, sessionId);
    }

    /**
     * Api để làm mới trạng thái online của người dùng
     *
     * @param userId  ID của người dùng cần làm mới trạng thái
     * @param request HttpServletRequest để lấy thông tin phiên làm việc
     * @return ResponseEntity chứa thông tin kết quả
     */
    @PostMapping("/refresh/{userId}")
    public ResponseEntity<Map<String, Object>> refreshOnlineStatus(
            @PathVariable UUID userId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                response.put("success", false);
                response.put("message", "No valid session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String sessionId = session.getId();
            onlineUserService.updateUserOnlineStatus(userId, sessionId);

            response.put("success", true);
            response.put("message", "Online status refreshed");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error refreshing online status for user: {}", userId, e);
            response.put("success", false);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @MessageMapping("/chat/typing")
    public void processTypingIndicator(TypingIndicatorDto typingDto, SimpMessageHeaderAccessor headerAccessor) {
        String userKey = RedisKeyBuilder.userIdAttributeKey();
        UUID userId = (UUID) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get(userKey);
        String sessionId = headerAccessor.getSessionId();

        if (typingDto.getConversationId() == null) {
            return;
        }

        if (userId != null && sessionId != null) {
            // Kiểm tra tính hợp lệ của phiên trước khi gửi thông báo
            if (redisTemplate.hasKey(RedisKeyBuilder.springSessionKey(sessionId))) {
                UserResponseDto user = userService.findById(userId);
                if (user != null) {
                    typingDto.setUserId(userId);
                    typingDto.setUserName(user.getFirstName() + " " + user.getLastName());

                }

                webSocketService.sendTypingIndicator(typingDto);
            } else {
                // Nếu phiên không hợp lệ thông báo lỗi
                webSocketService.sendSessionInvalidationNotification(sessionId, "SESSION_EXPIRED",
                        "Phiên làm việc đã hết hạn, vui lòng đăng nhập lại");
            }
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<OnlineUserStatusDto>> getOnlineUsers() {
        try {
            List<OnlineUserStatusDto> onlineUsers = onlineUserService.getOnlineUsers();
            return ResponseEntity.ok(onlineUsers);
        } catch (Exception e) {
            logger.error("Error getting online users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @GetMapping("/list-except-self")
    public ResponseEntity<List<OnlineUserStatusDto>> getOnlineUsersWithExceptSelf() {
        try {
            List<OnlineUserStatusDto> onlineUsers = onlineUserService.getOnlineUsersWithExceptSelf();
            return ResponseEntity.ok(onlineUsers);
        } catch (Exception e) {
            logger.error("Error getting online users except self", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @PostMapping("/offline/{userId}")
    public ResponseEntity<Map<String, Object>> markUserOffline(
            @PathVariable UUID userId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            HttpSession session = request.getSession(false);
            String sessionId = session != null ? session.getId() : null;

            onlineUserService.markUserOffline(userId, sessionId);

            response.put("success", true);
            response.put("message", "User marked offline");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error marking user offline: {}", userId, e);
            response.put("success", false);
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
