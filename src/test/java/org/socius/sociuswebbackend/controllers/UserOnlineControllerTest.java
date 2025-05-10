package org.socius.sociuswebbackend.controllers;

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
import org.socius.sociuswebbackend.services.WebSocketService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserOnlineControllerTest {
    @Mock
    private WebSocketService webSocketService;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private UserOnlineController userOnlineController;

    private UserEntity adminUser;
    private UserEntity regularUser;

    @BeforeEach
    void setUp() {
        adminUser = AuthTestDataUtil.createTestAdminUser();
        regularUser = AuthTestDataUtil.createTestRegularUser();
    }

    @Test
    @DisplayName("Xử lý heartbeat phải gọi webSocketService")
    void handleHeartbeatShouldCallWebSocketService() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", adminUser.getId());
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        userOnlineController.processHeartbeat(headerAccessor);

        verify(webSocketService).handleHeartbeat(adminUser.getId());
    }

    @Test
    @DisplayName("Xử lý heartbeat không làm gì khi không có userId")
    void handleHeartbeatShouldDoNothingWhenNoUserId() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        userOnlineController.processHeartbeat(headerAccessor);

        verify(webSocketService, never()).handleHeartbeat(any(UUID.class));
    }

}
