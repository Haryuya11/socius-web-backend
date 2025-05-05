package org.socius.sociuswebbackend.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.messages.SessionInvalidationMessage;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.socius.sociuswebbackend.services.SessionManagementService;
import org.socius.sociuswebbackend.websocket.WebSocketService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SessionInvalidationListener {
    private static final Logger logger = LoggerFactory.getLogger(SessionInvalidationListener.class);

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private RBACRedisService rbacRedisService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = "#{sessionInvalidationQueue.name}")
    public void handleSessionInvalidationMessage(SessionInvalidationMessage message) {
        logger.info("Nhận được thông điệp hủy phiên: {}", message);

        Set<String> sessionsToInvalidate = new HashSet<>();

        try {
            // TH1: Hủy tất cả phiên của người dùng thuộc một role cụ thể
            if (message.isForceAllUsersWithRole() && message.getRoleId() != null) {
                // Xóa cache quyền và lấy danh sách các phiên bị ảnh hưởng
                long count = rbacRedisService.deleteByRoleId(message.getRoleId());
                logger.info("Đã xóa {} bản ghi cache quyền hạn cho roleId: {}", count, message.getRoleId());

                // Tìm kiếm các phiên đang hoạt động của người dùng thuộc role này
                Set<String> roleSessions = sessionManagementService.getSessionsByRoleId(message.getRoleId());
                if (roleSessions != null && !roleSessions.isEmpty()) {
                    sessionsToInvalidate.addAll(roleSessions);
                }
            }
            // Trường hợp 2: Hủy các phiên cụ thể
            else if (message.getSessionIds() != null && !message.getSessionIds().isEmpty()) {
                sessionsToInvalidate.addAll(message.getSessionIds());
            }

            // Hủy các phiên đã tìm thấy
            if (!sessionsToInvalidate.isEmpty()) {
                for (String sessionId : sessionsToInvalidate) {
                    webSocketService.sendSessionInvalidationNotification(sessionId, message.getReason().toString(), message.getMessage());

                    rbacRedisService.deleteUserPermissions(sessionId);
                    sessionManagementService.invalidateSession(sessionId);
                }
                logger.info("Đã hủy các phiên: {}", sessionsToInvalidate);
            } else {
                logger.info("Không có phiên nào cần hủy.");
            }

        } catch (Exception e) {
            logger.error("Lỗi khi xử lý thông điệp hủy phiên: {}", e.getMessage(), e);
        }
    }
}
