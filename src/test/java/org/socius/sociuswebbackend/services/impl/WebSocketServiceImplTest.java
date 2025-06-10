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
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WebSocketServiceImplTest {
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketServiceImpl webSocketService;

    private UserEntity adminUser;

    @BeforeEach
    void setUp() {
        // Initialize test data
        adminUser = AuthTestDataUtil.createTestAdminUser();
    }

    @Test
    @DisplayName("Gửi thông báo đăng nhập của người dùng phải gửi qua WebSocket")
    void sendUserLoginNotificationShouldSendThroughWebSocket() {
        webSocketService.sendUserLoginNotification(adminUser.getFullName());

        verify(messagingTemplate).convertAndSend(anyString(), anyMap());
    }
}
