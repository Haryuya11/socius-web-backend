package org.socius.sociuswebbackend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.socius.sociuswebbackend.services.SessionManagementService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class SessionManagementServiceImpl implements SessionManagementService {

    private static final Logger logger = LoggerFactory.getLogger(SessionManagementServiceImpl.class);

    @Autowired
    private RedisIndexedSessionRepository sessionRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigService configService;

    @Autowired
    private RBACRedisService rbacRedisService;

    @Override
    public Set<String> getSessionsByRoleId(UUID roleId) {
        Set<String> sessionIds = new HashSet<>();
        try {
            Set<?> rawSet = redisTemplate.opsForSet().members(RedisKeyBuilder.roleUsersKey(roleId));
            if (rawSet != null) {
                for (Object obj : rawSet) {
                    if (obj instanceof String) {
                        sessionIds.add((String) obj);
                    }
                }
                logger.info("Đã tìm thấy {} phiên cho roleId: {}", sessionIds.size(), roleId);
            } else {
                logger.info("Không tìm thấy phiên nào cho roleId: {}", roleId);
                sessionIds = new HashSet<>();
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách phiên cho roleId {}: {}", roleId, e.getMessage(), e);
        }
        return sessionIds;
    }

    @Override
    public boolean invalidateSession(String sessionId) {
        try {
            try {
                sessionRepository.deleteById(sessionId);
            } catch (Exception e) {
                logger.warn("Không thể xóa phiên từ repository: {}", e.getMessage());
            }

            try {
                rbacRedisService.deleteUserPermissions(sessionId);
            } catch (Exception e) {
                logger.warn("Không thể xóa quyền người dùng: {}", e.getMessage());
            }

            redisTemplate.delete(RedisKeyBuilder.springSessionKey(sessionId));
            redisTemplate.delete(RedisKeyBuilder.springSessionExpiresKey(sessionId));

            logger.info("Đã xóa phiên {} khỏi Redis", sessionId);
            return true;
        } catch (Exception e) {
            logger.error("Lỗi khi hủy phiên {}: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }
}