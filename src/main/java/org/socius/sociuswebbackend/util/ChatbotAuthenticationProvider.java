package org.socius.sociuswebbackend.util;

import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.services.JwtTokenService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatbotAuthenticationProvider implements AuthenticationProvider {

    private final JwtTokenService jwtTokenService;

    public ChatbotAuthenticationProvider(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();

        try {
            UserEntity user = jwtTokenService.getUserFromToken(token);

            List<SimpleGrantedAuthority> authorities = user.getEmploymentDetail()
                    .getRole()
                    .getRolePermissions()
                    .stream()
                    .map(rp -> new SimpleGrantedAuthority(rp.getPermission().getName()))
                    .toList();

            return new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid chatbot token", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}
