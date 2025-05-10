package org.socius.sociuswebbackend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Service
public interface WebSocketService {

    /**
     * Gửi thông báo khi người dùng đăng nhập thành công
     */
    void sendUserLoginNotification(String username);

    /**
     * Gửi thông báo khi người dùng đăng xuất
     */
    void sendUserLogoutNotification(String username);

    /**
     * Gửi thông báo khi phiên làm việc bị hủy
     */
    void sendSessionInvalidationNotification(String sessionId, String reason, String message);

    /**
     * Xử lý heartbeat từ client
     */
    void handleHeartbeat(UUID userId);

    /**
     * Xử lý sự kiện ngắt kết nối tới WebSocket
     */
    void handleWebSocketDisconnectEvent(SessionDisconnectEvent event);
}
