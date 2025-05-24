package org.socius.sociuswebbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.TypingIndicatorDto;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.UserService;
import org.socius.sociuswebbackend.services.WebSocketService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
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
        try {
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes == null) {
                logger.warn("Session attributes null trong heartbeat");
                return;
            }

            UUID userId = (UUID) sessionAttributes.get("userId");
            String sessionId = headerAccessor.getSessionId();

            if (userId == null || sessionId == null) {
                logger.warn("UserId hoặc SessionId null trong heartbeat");
                return;
            }

            // Kiểm tra session validity
            if (isValidSession(sessionId)) {
                webSocketService.handleHeartbeat(userId);
            } else {
                handleInvalidSession(userId, sessionId);
            }
        } catch (Exception e) {
            logger.error("Lỗi xử lý heartbeat: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat/typing")
    public void processTypingIndicator(TypingIndicatorDto typingDto, SimpMessageHeaderAccessor headerAccessor) {
        UUID userId = (UUID) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId");
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

    /**
     * Kiểm tra tính hợp lệ của phiên làm việc dựa trên sessionId
     *
     * @param sessionId ID của phiên làm việc
     * @return true nếu phiên hợp lệ, false nếu không
     */
    private boolean isValidSession(String sessionId) {
        try {
            String sessionKey = RedisKeyBuilder.springSessionKey(sessionId);
            return redisTemplate.hasKey(sessionKey) && redisTemplate.getExpire(sessionKey) > 0;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra tính hợp lệ của phiên: {}", e.getMessage(), e);
            return false;
        }
    }

    private void handleInvalidSession(UUID userId, String sessionId) {
        logger.info("Session không hợp lệ, đánh dấu user {} offline", userId);
        onlineUserService.markUserOffline(userId, sessionId);
        webSocketService.sendSessionInvalidationNotification(sessionId, "SESSION_EXPIRED",
                "Phiên làm việc đã hết hạn, vui lòng đăng nhập lại");
    }
}
