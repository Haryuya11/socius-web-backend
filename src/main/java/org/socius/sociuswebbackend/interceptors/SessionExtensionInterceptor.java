package org.socius.sociuswebbackend.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.socius.sociuswebbackend.services.AuthenticationService;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class SessionExtensionInterceptor implements HandlerInterceptor {
    final private AuthenticationService authenticationService;
    final private ConfigService configService;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        HttpSession session = request.getSession();
        if (session != null) {

            // Lấy ngưỡng thời gian gia hạn từ cấu hình
            int extensionThresholdMinutes = configService.getInt("session_extension_threshold", 2);

            // Kiểm tra thời gian còn lại của phiên
            int remainingTime = session.getMaxInactiveInterval();
            if (remainingTime <= extensionThresholdMinutes * 60) {
                // Nếu thời gian còn lại nhỏ hơn ngưỡng, gia hạn phiên
                authenticationService.extendSession(request);
            }
        }
        return true;
    }
}
