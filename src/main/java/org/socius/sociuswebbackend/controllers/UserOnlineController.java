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
        UUID userId = (UUID) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId");
        String sessionId = headerAccessor.getSessionId();

        if (userId != null && sessionId != null) {
            // Kiểm tra tính hợp lệ của phiên trước khi kiểm tra heartbeat
            if (redisTemplate.hasKey(RedisKeyBuilder.springSessionKey(sessionId))) {
                webSocketService.handleHeartbeat(userId);
            } else {
                // Nếu phiên không hợp lệ, đánh dấu người dùng offline
                onlineUserService.markUserOffline(userId, sessionId);
                webSocketService.sendSessionInvalidationNotification(sessionId, "SESSION_EXPIRED",
                        "Phiên làm việc đã hết hạn, vui lòng đăng nhập lại");
            }

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
}
