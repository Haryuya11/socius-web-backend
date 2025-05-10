package org.socius.sociuswebbackend.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.socius.sociuswebbackend.utils.AuthTestDataUtil;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserOnlineWebSocketHandlerTest {
    @Mock
    private OnlineUserService onlineUserService;

    @Mock
    private WebSocketHandler wsHandler;

    @Mock
    private RBACRedisService rbacRedisService;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private ServletServerHttpRequest servletRequest;

    @InjectMocks
    private UserOnlineWebSocketHandler userOnlineWebSocketHandler;

    private UserEntity adminUser;
    private UserPermissionsDto adminPermissions;
    private MockHttpSession session;
    private MockHttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        // Initialize test data
        adminUser = AuthTestDataUtil.createTestAdminUser();
        adminPermissions = AuthTestDataUtil.createAdminPermissionsDto();

        session = new MockHttpSession();
        session.setAttribute("USER_ID", adminUser.getId());

        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setSession(session);

        when(servletRequest.getServletRequest()).thenReturn(httpServletRequest);
    }

    @Test
    @DisplayName("beforeHandshake phải trả về true khi có phiên và quyền hạn")
    void beforeHandshakeShouldReturnTrueWhenSessionAndPermissionsExist() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        when(rbacRedisService.getUserPermissions(anyString())).thenReturn(adminPermissions);

        boolean result = userOnlineWebSocketHandler.beforeHandshake(servletRequest, response, wsHandler, attributes);

        assertTrue(result, "Phải trả về true khi có phiên và quyền hạn");
        assertEquals(session.getId(), attributes.get("sessionId"), "sessionId không khớp");
        assertEquals(adminUser.getId(), attributes.get("userId"), "ID người dùng không khớp");
        verify(onlineUserService).updateUserOnlineStatus(adminUser.getId(), session.getId());
    }

    @Test
    @DisplayName("beforeHandshake phải trả về false khi không có phiên")
    void beforeHandshakeShouldReturnFalseWhenSessionNotExist() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        when(rbacRedisService.getUserPermissions(anyString())).thenReturn(null);

        boolean result = userOnlineWebSocketHandler.beforeHandshake(servletRequest, response, wsHandler, attributes);

        assertFalse(result, "Phải trả về false khi không có phiên");
        verify(onlineUserService, never()).updateUserOnlineStatus(any(UUID.class), anyString());
    }

    @Test
    @DisplayName("beforeHandshake phải trả về false khi không có quyền hạn")
    void beforeHandshakeShouldReturnFalseWhenPermissionsNotExist() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        when(rbacRedisService.getUserPermissions(anyString())).thenReturn(null);

        boolean result = userOnlineWebSocketHandler.beforeHandshake(servletRequest, response, wsHandler, attributes);

        assertFalse(result, "Phải trả về false khi không có quyền hạn");
        verify(onlineUserService, never()).updateUserOnlineStatus(any(UUID.class), anyString());
    }
}
