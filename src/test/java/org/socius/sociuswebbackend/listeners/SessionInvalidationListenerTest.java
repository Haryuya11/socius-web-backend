package org.socius.sociuswebbackend.listeners;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.enums.InvalidationReason;
import org.socius.sociuswebbackend.model.messages.SessionInvalidationMessage;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.socius.sociuswebbackend.services.SessionManagementService;
import org.socius.sociuswebbackend.websocket.WebSocketService;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SessionInvalidationListenerTest {
    @Mock
    private SessionManagementService sessionManagementService;

    @Mock
    private RBACRedisService rbacRedisService;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private SessionInvalidationListener sessionInvalidationListener;

    private final UUID roleId = UUID.randomUUID();
    private final String sessionId1 = "session-1";
    private final String sessionId2 = "session-2";
    private final Set<String> sessionIds = new HashSet<>();

    @BeforeEach
    void setUp() {
        sessionIds.add(sessionId1);
        sessionIds.add(sessionId2);
    }

    @Test
    @DisplayName("Xử lý thông điệp hủy phiên cho vai trò cụ thể")
    void handleSessionInvalidationMessageForRole() {
        SessionInvalidationMessage message = SessionInvalidationMessage.builder()
                .roleId(roleId)
                .reason(InvalidationReason.ROLE_CHANGED)
                .message("Vai trò đã thay đổi")
                .forceAllUsersWithRole(true)
                .build();

        when(sessionManagementService.getSessionsByRoleId(roleId)).thenReturn(sessionIds);

        sessionInvalidationListener.handleSessionInvalidationMessage(message);

        verify(rbacRedisService).deleteByRoleId(roleId);
        verify(sessionManagementService).getSessionsByRoleId(roleId);

        for (String sessionId : sessionIds) {
            verify(webSocketService).sendSessionInvalidationNotification(
                    eq(sessionId), eq(message.getReason().toString()), eq(message.getMessage()));

            verify(rbacRedisService).deleteByRoleId(roleId);
            verify(sessionManagementService).invalidateSession(sessionId);
        }
    }

    @Test
    @DisplayName("Xử lý thông điệp hủy phiên cụ thể")
    void handleSpecificSessionInvalidationMessage() {
        SessionInvalidationMessage message = SessionInvalidationMessage.builder()
                .sessionIds(sessionIds)
                .reason(InvalidationReason.FORCE_LOGOUT)
                .message("Phiên làm việc đã bị hủy")
                .forceAllUsersWithRole(false)
                .build();

        sessionInvalidationListener.handleSessionInvalidationMessage(message);

        for (String sessionId : sessionIds) {
            verify(webSocketService).sendSessionInvalidationNotification(
                    eq(sessionId), eq(message.getReason().toString()), eq(message.getMessage()));

            verify(rbacRedisService).deleteUserPermissions(sessionId);
            verify(sessionManagementService).invalidateSession(sessionId);
        }
    }

    @Test
    @DisplayName("Xử lý trường hợp không có phiên nào cần hủy")
    void handleSessionInvalidationMessageWithNoSessions() {
        SessionInvalidationMessage message = SessionInvalidationMessage.builder()
                .roleId(roleId)
                .reason(InvalidationReason.ROLE_CHANGED)
                .message("Vai trò đã thay đổi")
                .forceAllUsersWithRole(true)
                .build();

        when(sessionManagementService.getSessionsByRoleId(roleId)).thenReturn(new HashSet<>());

        sessionInvalidationListener.handleSessionInvalidationMessage(message);

        verify(rbacRedisService).deleteByRoleId(roleId);
        verify(sessionManagementService).getSessionsByRoleId(roleId);
        verify(webSocketService, never()).sendSessionInvalidationNotification(anyString(), anyString(), anyString());
        verify(rbacRedisService, never()).deleteUserPermissions(anyString());
        verify(sessionManagementService, never()).invalidateSession(anyString());
    }

    @Test
    @DisplayName("Xử lý ngoại lệ khi xử lý thông điệp")
    void handleExceptionDuringMessageProcessing() {
        SessionInvalidationMessage message = SessionInvalidationMessage.builder()
                .roleId(roleId)
                .reason(InvalidationReason.ROLE_CHANGED)
                .message("Vai trò đã thay đổi")
                .forceAllUsersWithRole(true)
                .build();

        when(sessionManagementService.getSessionsByRoleId(roleId)).thenThrow(new RuntimeException("Lỗi test"));

        sessionInvalidationListener.handleSessionInvalidationMessage(message);

        verify(rbacRedisService).deleteByRoleId(roleId);
    }
}
