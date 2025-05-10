package org.socius.sociuswebbackend.controllers;

import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UserOnlineController {
    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    WebSocketService webSocketService;

    /**
     * Xử lý thông điệp từ client để cập nhật trạng thái hoạt động của người dùng
     *
     * @param headerAccessor Thông tin tiêu đề của thông điệp
     */
    @MessageMapping("/heartbeat")
    public void processHeartbeat(SimpMessageHeaderAccessor headerAccessor) {
        UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");
        if (userId != null) {
            webSocketService.handleHeartbeat(userId);
        }
    }
}
