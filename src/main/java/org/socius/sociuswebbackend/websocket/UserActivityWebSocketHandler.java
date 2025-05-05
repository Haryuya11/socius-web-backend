package org.socius.sociuswebbackend.websocket;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class UserActivityWebSocketHandler implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserActivityWebSocketHandler.class);

    @Autowired
    private OnlineUserService onlineUserService;

    @Autowired
    private RBACRedisService rbacRedisService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            HttpSession session = servletRequest.getSession(false);

            if (session != null) {
                String sessionId = session.getId();
                attributes.put("sessionId", sessionId);

                UserPermissionsDto userPermissions = rbacRedisService.getUserPermissions(sessionId);
                if (userPermissions != null && userPermissions.getUserId() != null) {
                    attributes.put("userId", userPermissions.getUserId());
                    onlineUserService.updateUserOnlineStatus(userPermissions.getUserId(), sessionId);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("Lỗi khi thực hiện bắt tay với WebSocket: {}", exception.getMessage(), exception);
        } else {
            logger.info("Đã bắt tay thành công với WebSocket");
        }
    }
}
