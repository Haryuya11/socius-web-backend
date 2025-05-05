package org.socius.sociuswebbackend.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.socius.sociuswebbackend.services.AuthenticationService;
import org.socius.sociuswebbackend.services.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionExtensionInterceptor implements HandlerInterceptor {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ConfigService configService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
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
