package org.socius.sociuswebbackend.services.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Service
public class WebSocketServiceImpl implements WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServiceImpl.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private OnlineUserService onlineUserService;

    /**
     * Gửi thông báo khi người dùng đăng nhập thành công
     */
    @Override
    public void sendUserLoginNotification(String username) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "USER_LOGIN");
            notification.put("username", username);
            notification.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/notifications", notification);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông báo đăng nhập: {}", e.getMessage(), e);
        }
    }

    /**
     * Gửi thông báo khi người dùng đăng xuất
     */
    @Override
    public void sendUserLogoutNotification(String username) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "USER_LOGOUT");
            notification.put("username", username);
            notification.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/notifications", notification);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông báo đăng xuất: {}", e.getMessage(), e);
        }
    }

    /**
     * Gửi thông báo khi phiên làm việc bị hủy
     */
    @Override
    public void sendSessionInvalidationNotification(String sessionId, String reason, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SESSION_INVALIDATION");
            notification.put("reason", reason);
            notification.put("message", message);
            notification.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSendToUser(
                    sessionId,
                    "/topic/notifications",
                    notification
            );

            messagingTemplate.convertAndSend("/topic/notifications", notification);
        } catch (Exception e) {
            logger.error("Lỗi khi gửi thông báo hủy phiên: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý heartbeat từ client
     */
    @Override
    public void handleHeartbeat(UUID userId) {
        try {
            onlineUserService.handleUserHeartbeat(userId);
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý heartbeat: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý sự kiện ngắt kết nối tới WebSocket
     */
    @Override
    @EventListener
    public void handleWebSocketDisconnectEvent(SessionDisconnectEvent event) {
        // Lấy thông tin phiên làm việc từ sự kiện
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null && sessionId != null) {
            onlineUserService.markUserOffline(userId, sessionId);
            logger.info("Người dùng {} đã ngắt kết nối với websocket", userId);
        }
    }
}
