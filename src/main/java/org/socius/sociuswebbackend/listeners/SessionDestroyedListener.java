package org.socius.sociuswebbackend.listeners;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.socius.sociuswebbackend.services.WebSocketService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.events.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionDestroyedListener {
    private static final Logger logger = LoggerFactory.getLogger(SessionDestroyedListener.class);

    final private RBACRedisService rbacRedisService;
    final private RedisTemplate<String, Object> redisTemplate;
    final private WebSocketService webSocketService;
    final private OnlineUserService onlineUserService;

    /**
     * Xử lý sự kiện khi một phiên làm việc bị hủy
     *
     * @param event Sự kiện chứa thông tin về phiên làm việc đã bị hủy
     */
    @EventListener
    public void handleSessionDestroyedEvent(SessionDestroyedEvent event) {
        String sessionId = event.getSessionId();
        logger.info("Nhận được sự kiện hủy phiên: {}", event.getSessionId());

        // Lấy userId từ sessionId
        String userIdKey = RedisKeyBuilder.sessionUserKey(sessionId);
        Object userIdObj = redisTemplate.opsForValue().get(userIdKey);
        UUID userId = userIdObj instanceof UUID ? (UUID) userIdObj : null;

        if (userIdObj != null) {

            onlineUserService.markUserOffline(userId, sessionId);

            // Thông báo cho client WebSocket rằng phiên đã hết hạn
            webSocketService.sendSessionInvalidationNotification(sessionId, "SESSION_DESTROYED",
                    "Phiên làm việc đã kết thúc, vui lòng đăng nhập lại");

            logger.info("Đánh dấu người dùng {} là offline do phiên {} đã bị hủy", userId, sessionId);
        }

        // Xóa RBAC permissions
        rbacRedisService.deleteUserPermissions(event.getSessionId());

        // Xóa dữ liệu liên quan đến phiên làm việc trong Redis
        cleanupSessionData(sessionId);
    }

    private void cleanupSessionData(String sessionId) {
        try {
            // Xóa các key liên quan đến session
            redisTemplate.delete(RedisKeyBuilder.springSessionKey(sessionId));
            redisTemplate.delete(RedisKeyBuilder.springSessionExpiresKey(sessionId));
            redisTemplate.delete(RedisKeyBuilder.sessionUserKey(sessionId));

            logger.debug("Đã dọn dẹp dữ liệu session: {}", sessionId);
        } catch (Exception e) {
            logger.error("Lỗi khi dọn dẹp session data: {}", e.getMessage(), e);
        }
    }
}
