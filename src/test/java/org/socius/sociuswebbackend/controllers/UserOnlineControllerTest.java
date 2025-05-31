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
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.WebSocketService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private OnlineUserService onlineUserService;

    @InjectMocks
    private UserOnlineController userOnlineController;

    private UserEntity adminUser;

    @BeforeEach
    void setUp() {
        adminUser = AuthTestDataUtil.createTestAdminUser();
    }

    @Test
    @DisplayName("Xử lý heartbeat phải gọi webSocketService")
    void handleHeartbeatShouldCallWebSocketService() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", adminUser.getId());
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        String mockSessionId = "test-session-id";
        when(headerAccessor.getSessionId()).thenReturn(mockSessionId);
        when(redisTemplate.hasKey(RedisKeyBuilder.springSessionKey(mockSessionId))).thenReturn(true);
        when(redisTemplate.getExpire(RedisKeyBuilder.springSessionKey(mockSessionId))).thenReturn(1L);

        userOnlineController.processHeartbeat(headerAccessor);

        verify(onlineUserService).handleUserHeartbeat(adminUser.getId());
    }

    @Test
    @DisplayName("Xử lý heartbeat không làm gì khi không có userId")
    void handleHeartbeatShouldDoNothingWhenNoUserId() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        userOnlineController.processHeartbeat(headerAccessor);

        verify(onlineUserService, never()).handleUserHeartbeat(any(UUID.class));
    }

    @Test
    @DisplayName("Xử lý heartbeat phải đánh dấu offline khi session không hợp lệ")
    void handleHeartbeatShouldMarkOfflineWhenSessionInvalid() {
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", adminUser.getId());
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);

        String mockSessionId = "invalid-session-id";
        when(headerAccessor.getSessionId()).thenReturn(mockSessionId);
        when(redisTemplate.hasKey(RedisKeyBuilder.springSessionKey(mockSessionId))).thenReturn(false);

        userOnlineController.processHeartbeat(headerAccessor);

        verify(onlineUserService, never()).handleUserHeartbeat(any(UUID.class));
        verify(onlineUserService).markUserOffline(adminUser.getId(), mockSessionId);
        verify(webSocketService).sendSessionInvalidationNotification(
                eq(mockSessionId),
                eq("SESSION_EXPIRED"),
                anyString()
        );
    }
}
