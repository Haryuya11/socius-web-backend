package org.socius.sociuswebbackend.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.user.UserResponseDto;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatbotAuthenticationFilter implements Filter {

    private final UserService userService;
    private final ConfigService configService;
    private static final Logger logger = LoggerFactory.getLogger(ChatbotAuthenticationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        // Chỉ áp dụng cho chatbot endpoints
        if (!requestURI.startsWith("/api/chatbot/")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // Validate chatbot secret key
            String chatbotSecret = httpRequest.getHeader("X-Chatbot-Secret");
            String expectedSecret = configService.getString("chatbot.secret.key");

            if (chatbotSecret == null || !chatbotSecret.equals(expectedSecret)) {
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            // Get user ID from header
            String userIdHeader = httpRequest.getHeader("X-Chatbot-User-Id");
            if (userIdHeader == null) {
                httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
                return;
            }

            UUID userId = UUID.fromString(userIdHeader);

            // Validate user exists
            UserResponseDto user = userService.findById(userId);
            if (user == null) {
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            // Set user context for permission checking
            request.setAttribute("chatbot.user.id", userId);

            chain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Error in chatbot authentication filter", e);
            httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}