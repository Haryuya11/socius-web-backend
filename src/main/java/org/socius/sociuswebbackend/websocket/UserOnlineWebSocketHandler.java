package org.socius.sociuswebbackend.websocket;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserOnlineWebSocketHandler implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserOnlineWebSocketHandler.class);
    private final OnlineUserService onlineUserService;
    private final RBACRedisService rbacRedisService;

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes
    ) throws Exception {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            HttpSession session = httpRequest.getSession(false);

            logger.info("WebSocket handshake attempt from: {}", request.getRemoteAddress());

            if (session == null) {
                logger.warn("No HTTP session found for WebSocket connection");
                return false;
            }

            String sessionId = session.getId();
            logger.debug("Session ID: {}", sessionId);

            // Kiểm tra user permissions từ Redis
            UserPermissionsDto userPermissions = rbacRedisService.getUserPermissions(sessionId);
            if (userPermissions == null) {
                logger.warn("No user permissions found for session: {}", sessionId);
                return false;
            }

            UUID userId = userPermissions.getUserId();
            if (userId == null) {
                logger.warn("No user ID found in permissions for session: {}", sessionId);
                return false;
            }

            // Lưu thông tin vào attributes để sử dụng sau
            attributes.put("userId", userId.toString());
            attributes.put("sessionId", sessionId);

            logger.info("WebSocket handshake approved for user: {} with session: {}", userId, sessionId);
            return true;
        }

        logger.warn("Request is not a ServletServerHttpRequest");
        return false;
    }

    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            Exception exception
    ) {
        if (exception != null) {
            logger.error("WebSocket handshake failed", exception);
        } else {
            logger.info("WebSocket handshake completed successfully");
        }
    }
}