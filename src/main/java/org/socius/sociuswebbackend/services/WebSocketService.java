package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.message.TypingIndicatorDto;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

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
     * Xử lý sự kiện ngắt kết nối tới WebSocket
     */
    void handleWebSocketDisconnectEvent(SessionDisconnectEvent event);

    /**
     * Gửi thông báo khi người dùng đang gõ
     *
     * @param typingDto Thông tin về trạng thái gõ
     */
    void sendTypingIndicator(TypingIndicatorDto typingDto);

    /**
     * Gửi thông báo khi người dùng đã kết nối tới WebSocket
     *
     * @param event Sự kiện kết nối
     */
    void handleWebSocketConnectEvent(SessionConnectedEvent event);

    /**
     * Kiểm tra tính hợp lệ của phiên và thông báo cho client nếu phiên không hợp lệ
     *
     * @param sessionId ID phiên cần kiểm tra
     * @return true nếu phiên hợp lệ, false nếu không hợp lệ
     */
    boolean validateSessionAndNotify(String sessionId);
}
