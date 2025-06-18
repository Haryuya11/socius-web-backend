package org.socius.sociuswebbackend.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.services.JwtTokenService;
import org.socius.sociuswebbackend.util.ChatbotAuthenticationProvider;
import org.socius.sociuswebbackend.util.ChatbotAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ChatbotAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotAuthenticationFilter.class);
    private final ChatbotAuthenticationProvider authProvider;
    private final JwtTokenService jwtTokenService;

    public ChatbotAuthenticationFilter(ChatbotAuthenticationProvider authProvider, JwtTokenService jwtTokenService) {
        this.authProvider = authProvider;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Chỉ áp dụng filter cho /api/chatbot endpoints
        if (!request.getRequestURI().startsWith("/api/chatbot")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && !authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                // Xác thực token
                ChatbotAuthenticationToken authToken = new ChatbotAuthenticationToken(token);
                Authentication authentication = authProvider.authenticate(authToken);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                UserEntity user = jwtTokenService.getUserFromToken(token);
                request.setAttribute("CHATBOT_USER", user);
            } catch (Exception e) {
                logger.error("Chatbot authentication failed: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid chatbot token");
                return;
            }
        } else {
            logger.warn("No chatbot token provided in request");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
