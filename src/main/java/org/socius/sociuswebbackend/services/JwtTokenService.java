package org.socius.sociuswebbackend.services;

import io.jsonwebtoken.Claims;
import org.socius.sociuswebbackend.model.entities.UserEntity;

import java.util.UUID;

public interface JwtTokenService {
    /**
     * Tạo JWT token cho người dùng
     * @param userId ID của người dùng
     * @return JWT token dưới dạng chuỗi
     */
    String generateChatbotToken(UUID userId);

    Claims getClaimsFromToken(String token);

    UserEntity getUserFromToken(String token);
}
