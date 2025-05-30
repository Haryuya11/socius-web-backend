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
    ) {
        try {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                HttpServletRequest httpRequest = servletRequest.getServletRequest();
                HttpSession httpSession = httpRequest.getSession(false);

                if (httpSession == null) {
                    logger.warn("Không tìm thấy HTTP session trong yêu cầu WebSocket");
                    return false;
                }

                String sessionId = httpSession.getId();
                UUID userId = (UUID) httpSession.getAttribute("userId");

                if (userId == null) {
                    logger.warn("Không tìm thấy userId trong HTTP session");
                    return false;
                }

                // Lưu thông tin vào websocket attributes
                attributes.put("userId", userId.toString());
                attributes.put("sessionId", sessionId);

                logger.info("WebSocket handshake thành công cho userId: {}, sessionId: {}", userId, sessionId);
                return true;
            }

            logger.warn("Request không phải là ServletServerHttpRequest");
            return false;
        } catch (Exception e) {
            logger.error("Lỗi trong quá trình xử lý trước khi bắt tay WebSocket: {}", e.getMessage(), e);
            return false;
        }
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