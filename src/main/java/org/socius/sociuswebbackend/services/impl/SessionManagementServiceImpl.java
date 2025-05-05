package org.socius.sociuswebbackend.services.impl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.RBACRedisService;
import org.socius.sociuswebbackend.services.SessionManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;

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
            String roleUserPrefix = configService.getString("rbac.role.users.prefix", "role:users:");
            String roleKey = roleUserPrefix + roleId.toString();
            sessionIds = (Set<String>) (Set<?>) redisTemplate.opsForSet().members(roleKey);
            if (sessionIds != null) {
                logger.info("Đã tìm thấy {} phiên cho roleId: {}", sessionIds.size(), roleId);
            } else {
                logger.info("Không tìm thấy phiên nào cho roleId: {}", roleId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách phiên cho roleId {}: {}", roleId, e.getMessage(), e);
        }
        return sessionIds;
    }

    @Override
    public boolean invalidateSession(String sessionId) {
        try {
            Session session = sessionRepository.findById(sessionId);
            if (session == null) {
                logger.warn("Không tìm thấy phiên với sessionId: {}", sessionId);
                return false;
            }

            sessionRepository.deleteById(sessionId);
            rbacRedisService.deleteUserPermissions(sessionId);
            String sessionPrefix = "spring:session:";
            redisTemplate.delete(sessionPrefix + "sessions:" + sessionId);
            redisTemplate.delete(sessionPrefix + "sessions:expires:" + sessionId);

            logger.info("Đã xóa phiên {} khỏi Redis", sessionId);
            return true;
        } catch (Exception e) {
            logger.error("Lỗi khi hủy phiên {}: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }
}