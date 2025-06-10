package org.socius.sociuswebbackend.websocket;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketCsrfInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketCsrfInterceptor.class);

    private final CsrfTokenRepository csrfTokenRepository;

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {
        // Kiểm tra nếu request không phải là ServletServerHttpRequest
        if (!(request instanceof ServletServerHttpRequest)) {
            logger.warn("Không thể xác thực CSRF cho kết nối không phải ServletServerHttpRequest");
            return false;
        }

        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        String requestURI = servletRequest.getRequestURI();
        if (requestURI.contains("/xhr") || requestURI.contains("/jsonp") || requestURI.contains("/iframe")) {
            logger.debug("Cho phép SockJS fallback bypass CSRF: {}", requestURI);
            return true;
        }

        // Lấy CSRF token từ nhiều nguồn
        String csrfToken;

        // 1. Từ header X-CSRF-TOKEN
        csrfToken = servletRequest.getHeader("X-CSRF-TOKEN");

        // 2. Từ cookie XSRF-TOKEN
        if (csrfToken == null) {
            Cookie[] cookies = servletRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("XSRF-TOKEN".equals(cookie.getName())) {
                        csrfToken = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // 3. Từ query parameter
        if (csrfToken == null) {
            csrfToken = servletRequest.getParameter("X-CSRF-TOKEN");
        }

        if (csrfToken == null) {
            logger.warn("Không tìm thấy CSRF token trong request");
            return false;
        }

        CsrfToken expectedToken = csrfTokenRepository.loadToken(servletRequest);

        if (expectedToken == null) {
            logger.warn("Không tìm thấy CSRF token trong yêu cầu");
            return false;
        }

        if (!csrfToken.equals(expectedToken.getToken())) {
            logger.warn("CSRF token không khớp. Expected: {}, Actual: {}",
                    expectedToken.getToken(), csrfToken);
            return false;
        }

        logger.debug("CSRF token xác thực thành công cho kết nối WebSocket");
        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            logger.error("Lỗi khi thực hiện bắt tay với WebSocket: {}", exception.getMessage(), exception);
        } else {
            logger.info("Đã bắt tay thành công với WebSocket");
        }
    }
}
