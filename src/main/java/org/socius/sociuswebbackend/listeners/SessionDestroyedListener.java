package org.socius.sociuswebbackend.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.events.SessionDestroyedEvent;
import org.springframework.stereotype.Component;

@Component
public class SessionDestroyedListener {
    private static final Logger logger = LoggerFactory.getLogger(SessionDestroyedListener.class);

    @Autowired
    private RBACRedisService rbacRedisService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @EventListener
    public void handleSessionDestroyedEvent(SessionDestroyedEvent event) {
        logger.info("Nhận được sự kiện hủy phiên: {}", event.getSessionId());

        rbacRedisService.deleteUserPermissions(event.getSessionId());

        String sessionPrefix = "spring:session:";
        redisTemplate.delete(sessionPrefix + "sessions:" + event.getSessionId());
        redisTemplate.delete(sessionPrefix + "sessions:expires:" + event.getSessionId());

        logger.info("Đã dọn dẹp dữ liệu Redis cho session: {}", event.getSessionId());
    }
}
