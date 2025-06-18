package org.socius.sociuswebbackend.services.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.JwtTokenService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenServiceImpl.class);
    private final ConfigService configService;
    private final UserRepository userRepository;

    private String getJwtSecret() {
        return configService.getString("jwt.secret");
    }

    private int getJwtExpirationMs() {
        return configService.getInt("jwt.expiration.ms", 3600000);
    }

    @Override
    public String generateChatbotToken(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("role", user.getEmploymentDetail().getRole().getName())
                .claim("type", "CHATBOT_TOKEN")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + getJwtExpirationMs()))
                .signWith(SignatureAlgorithm.HS256, getJwtSecret())
                .compact();
    }

    @Override
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getJwtSecret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", token, e);
            return null;
        }
    }

    @Override
    public UserEntity getUserFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String email = claims.getSubject();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));
    }
}
