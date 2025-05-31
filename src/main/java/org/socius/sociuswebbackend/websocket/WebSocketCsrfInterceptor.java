package org.socius.sociuswebbackend.websocket;

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
        CsrfToken csrfToken = csrfTokenRepository.loadToken(servletRequest);

        if (csrfToken == null) {
            logger.warn("Không tìm thấy CSRF token trong yêu cầu");
            return false;
        }

        // Lấy header X-CSRF-Token từ yêu cầu
        String csrfHeader = servletRequest.getHeader(csrfToken.getHeaderName());

        // Nếu không có trong header, tìm trong tham số truy vấn
        if (csrfHeader == null || csrfHeader.isEmpty()) {
            csrfHeader = servletRequest.getParameter(csrfToken.getParameterName());
        }

        // Kiểm tra xem CSRF token có hợp lệ không
        if (csrfHeader == null || !csrfHeader.equals(csrfToken.getToken())) {
            logger.warn("CSRF token không hợp lệ");
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
