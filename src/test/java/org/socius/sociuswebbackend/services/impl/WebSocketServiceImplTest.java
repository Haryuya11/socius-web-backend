package org.socius.sociuswebbackend.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WebSocketServiceImplTest {
    @Mock
    private OnlineUserService onlineUserService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketServiceImpl webSocketService;

    private UserEntity adminUser;
    private UserEntity regularUser;

    @BeforeEach
    void setUp() {
        // Initialize test data
        adminUser = AuthTestDataUtil.createTestAdminUser();
        regularUser = AuthTestDataUtil.createTestRegularUser();
    }

    @Test
    @DisplayName("Xử lý heartbeat phải gọi service xử lý heartbeat")
    void handleHeartbeatShouldCallOnlineUserService() {
        webSocketService.handleHeartbeat(adminUser.getId());

        verify(onlineUserService).handleUserHeartbeat(adminUser.getId());
    }

    @Test
    @DisplayName("Xử lý heartbeat phải bắt ngoại lệ nếu có lỗi")
    void handleHeartbeatShouldThrowException() {

        doThrow(new RuntimeException("Test Exception")).when(onlineUserService).handleUserHeartbeat(any(UUID.class));

        webSocketService.handleHeartbeat(regularUser.getId());

        verify(onlineUserService).handleUserHeartbeat(regularUser.getId());
    }

    @Test
    @DisplayName("Gửi thông báo đăng nhập của người dùng phải gửi qua WebSocket")
    void sendUserLoginNotificationShouldSendThroughWebSocket() {
        webSocketService.sendUserLoginNotification(adminUser.getFullName());

        verify(messagingTemplate).convertAndSend(anyString(), anyMap());
    }
}
