package org.socius.sociuswebbackend.websocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo khi người dùng đăng nhập thành công
     */
    public void sendUserLoginNotification(String username) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "USER_LOGIN");
        notification.put("username", username);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Gửi thông báo khi người dùng đăng xuất
     */
    public void sendUserLogoutNotification(String username) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "USER_LOGOUT");
        notification.put("username", username);
        notification.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}
