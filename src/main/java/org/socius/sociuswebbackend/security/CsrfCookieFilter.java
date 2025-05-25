package org.socius.sociuswebbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CsrfCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        // Kiểm tra xem token có tồn tại không
        if (csrfToken != null) {
            response.setHeader("X-CSRF-HEADER", csrfToken.getHeaderName());
            response.setHeader("X-CSRF-PARAM", csrfToken.getParameterName());
        }
        filterChain.doFilter(request, response);
    }
}
