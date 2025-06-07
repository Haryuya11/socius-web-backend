package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.services.SessionValidationService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionValidationServiceImpl implements SessionValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionValidationServiceImpl.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public boolean isSessionValid(String sessionId) {
        try {
            String rbacKey = RedisKeyBuilder.rbacKey(sessionId);
            Boolean exists = redisTemplate.hasKey(rbacKey);
            
            if (exists) {
                // Kiểm tra xem key có expire time không (nếu có nghĩa là session còn hợp lệ)
                Long expireTime = redisTemplate.getExpire(rbacKey);
                return expireTime > 0;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra session validity cho sessionId {}: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean hasValidSession(UUID userId) {
        try {
            // Tìm tất cả RBAC keys
            Set<String> rbacKeys = redisTemplate.keys(RedisKeyBuilder.rbacPattern());
            
            if (rbacKeys.isEmpty()) {
                return false;
            }
            
            // Kiểm tra từng key để tìm session của userId
            for (String rbacKey : rbacKeys) {
                try {
                    UserPermissionsDto permissions = (UserPermissionsDto) redisTemplate.opsForValue().get(rbacKey);
                    
                    if (permissions != null && userId.equals(permissions.getUserId())) {
                        // Kiểm tra xem key này có expire time không
                        Long expireTime = redisTemplate.getExpire(rbacKey);
                        if (expireTime > 0) {
                            logger.debug("Tìm thấy session hợp lệ cho userId: {}", userId);
                            return true;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Lỗi khi đọc RBAC key {}: {}", rbacKey, e.getMessage());
                    // Continue checking other keys
                }
            }
            
            logger.debug("Không tìm thấy session hợp lệ cho userId: {}", userId);
            return false;
            
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra session validity cho userId {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getUserSessionId(UUID userId) {
        try {
            // Tìm tất cả RBAC keys
            Set<String> rbacKeys = redisTemplate.keys(RedisKeyBuilder.rbacPattern());
            
            if (rbacKeys.isEmpty()) {
                return null;
            }
            
            // Kiểm tra từng key để tìm session của userId
            for (String rbacKey : rbacKeys) {
                try {
                    UserPermissionsDto permissions = (UserPermissionsDto) redisTemplate.opsForValue().get(rbacKey);
                    
                    if (permissions != null && userId.equals(permissions.getUserId())) {
                        // Kiểm tra xem key này có expire time không
                        Long expireTime = redisTemplate.getExpire(rbacKey);
                        if (expireTime > 0) {
                            // Extract sessionId from rbac key: "rbac:session:{sessionId}"
                            return rbacKey.substring("rbac:session:".length());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Lỗi khi đọc RBAC key {}: {}", rbacKey, e.getMessage());
                    // Continue checking other keys
                }
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("Lỗi khi tìm sessionId cho userId {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}