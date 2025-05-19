package org.socius.sociuswebbackend.listeners;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.socius.sociuswebbackend.services.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.events.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SessionDestroyedListener {
    private static final Logger logger = LoggerFactory.getLogger(SessionDestroyedListener.class);

    final private RBACRedisService rbacRedisService;
    final private RedisTemplate<String, Object> redisTemplate;
    final private WebSocketService webSocketService;

    @EventListener
    public void handleSessionDestroyedEvent(SessionDestroyedEvent event) {
        String sessionId = event.getSessionId();
        logger.info("Nhận được sự kiện hủy phiên: {}", event.getSessionId());

        // Lấy userId từ sessionId
        String userIdKey = "SESSION:" + sessionId + ":USER_ID";
        Object userIdObj = redisTemplate.opsForValue().get(userIdKey);

        if (userIdObj != null) {
            String reconnectPattern = "reconnect:*:" + sessionId;
            Set<String> keys = redisTemplate.keys(reconnectPattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
            }

            // Thông báo cho client WebSocket rằng phiên đã hết hạn
            webSocketService.sendSessionInvalidationNotification(sessionId, "SESSION_DESTROYED",
                    "Phiên làm việc đã kết thúc, vui lòng đăng nhập lại");
        }

        rbacRedisService.deleteUserPermissions(event.getSessionId());

        String sessionPrefix = "spring:session:";
        redisTemplate.delete(sessionPrefix + "sessions:" + event.getSessionId());
        redisTemplate.delete(sessionPrefix + "sessions:expires:" + event.getSessionId());

        logger.info("Đã dọn dẹp dữ liệu Redis cho session: {}", event.getSessionId());
    }
}
