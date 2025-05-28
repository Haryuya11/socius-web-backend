package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.auth.UserPermissionsDto;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.RedisCleanupService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisCleanupServiceImpl implements RedisCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCleanupServiceImpl.class);

    private static final String RBAC_PREFIX = "rbac:";
    private static final String CACHE_PREFIX = "cache:";


    final private RedisTemplate<String, Object> redisTemplate;

    final private ConfigService configService;


    @Override
    @Scheduled(cron = "0 */10 * * * *")
    public void cleanupExpiredOnlineStatus() {
        try {
            // Lấy thời gian timeout cho online status (khác với session timeout)
            int onlineTimeoutMinutes = configService.getInt("online.status.timeout.minutes", 5); // Mặc định là 5 phút

            Set<String> onlineKeys = redisTemplate.keys(RedisKeyBuilder.userOnlinePattern());
            if (!onlineKeys.isEmpty()) {
                for (String key : onlineKeys) {
                    OnlineUserStatusDto status = (OnlineUserStatusDto) redisTemplate.opsForValue().get(key);

                    if (status != null) {
                        // Kiểm tra xem người dùng đã không hoạt động quá lâu chưa
                        boolean isExpiredOnlineStatus = isOnlineStatusExpired(status.getLastSeen(), onlineTimeoutMinutes);

                        if (isExpiredOnlineStatus) {
                            // Kiểm tra phiên còn hợp lệ không
                            boolean isSessionValid = isSessionValid(status.getSessionId());

                            if (isSessionValid) {
                                // Phiên còn hợp lệ nhưng người dùng không hoạt động lâu
                                // -> Chỉ đánh dấu offline, KHÔNG xóa online status
                                status.setOnline(false);
                                redisTemplate.opsForValue().set(key, status);
                                logger.info("Người dùng {} đã không hoạt động quá lâu, đánh dấu offline nhưng giữ online status", status.getUserId());
                            } else {
                                // Phiên đã hết hạn -> Xóa online status
                                redisTemplate.delete(key);
                                logger.info("Đã xóa online status cho người dùng {} do phiên đã hết hạn", status.getUserId());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi dọn dẹp online status: {}", e.getMessage(), e);
        }
    }


    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredUserPermissions() {
        try {
            Set<String> redisKeys = redisTemplate.keys(RedisKeyBuilder.getKeyPattern(RBAC_PREFIX));
            if (!redisKeys.isEmpty()) {
                int count = 0;
                for (String redisKey : redisKeys) {
                    long ttl = redisTemplate.getExpire(redisKey);

                    // -1: TTL không xác định, -2: key không tồn tại
                    if (ttl <= 0 && ttl != -1) {
                        UserPermissionsDto userPermissionsDto = (UserPermissionsDto) redisTemplate.opsForValue().get(redisKey);
                        redisTemplate.delete(redisKey);
                        count++;
                        if (userPermissionsDto != null) {
                            logger.debug("Đã dọn dẹp quyền hạn hết hạn cho người dùng: {}", userPermissionsDto.getUserId());
                        } else {
                            logger.debug("Đã dọn dẹp quyền hạn hết hạn cho key: {}", redisKey);
                        }
                    }
                }
                if (count > 0) {
                    logger.info("Đã dọn dẹp {} quyền hạn hết hạn", count);
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi dọn dẹp quyền truy cập hết hạn: {}", e.getMessage(), e);
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    public void cleanupExpiredSession() {
        try {
            Set<String> sessionKeys = redisTemplate.keys(RedisKeyBuilder.springSessionPattern());
            if (!sessionKeys.isEmpty()) {
                for (String sessionKey : sessionKeys) {
                    Boolean exists = redisTemplate.hasKey(sessionKey);
                    Long expireTime = redisTemplate.getExpire(sessionKey);

                    // Nếu session không tồn tại hoặc đã hết hạn
                    if (!exists || expireTime <= 0) {
                        // Tìm và xóa online status tương ứng
                        String sessionId = extractSessionIdFromKey(sessionKey);
                        removeOnlineStatusBySessionId(sessionId);

                        logger.info("Đã xóa session hết hạn: {}", sessionId);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi dọn dẹp session: {}", e.getMessage(), e);
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredCache() {
        try {
            Set<String> redisKeys = redisTemplate.keys(RedisKeyBuilder.getKeyPattern(CACHE_PREFIX));
            if (!redisKeys.isEmpty()) {
                int count = 0;
                for (String redisKey : redisKeys) {
                    long ttl = redisTemplate.getExpire(redisKey);
                    if (ttl <= 0 && ttl != -1) {
                        redisTemplate.delete(redisKey);
                        count++;
                    }
                }
                if (count > 0) {
                    logger.info("Đã dọn dẹp {} cache hết hạn", count);
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi dọn dẹp cache hết hạn: {}", e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra trạng thái online đã hết hạn chưa (dựa trên thời gian không hoạt động)
     */
    private boolean isOnlineStatusExpired(LocalDateTime lastSeen, int timeoutMinutes) {
        if (lastSeen == null) return true;

        LocalDateTime now = LocalDateTime.now();
        Duration timeSinceLastActivity = Duration.between(lastSeen, now);

        return timeSinceLastActivity.toMinutes() > timeoutMinutes;
    }

    /**
     * Kiểm tra phiên còn hợp lệ không
     */
    private boolean isSessionValid(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return false;

        try {
            String sessionKey = RedisKeyBuilder.springSessionKey(sessionId);
            Boolean exists = redisTemplate.hasKey(sessionKey);
            Long expireTime = redisTemplate.getExpire(sessionKey);

            return exists && expireTime > 0;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra session validity: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Xóa online status theo sessionId
     */
    private void removeOnlineStatusBySessionId(String sessionId) {
        try {
            Set<String> onlineKeys = redisTemplate.keys(RedisKeyBuilder.userOnlinePattern());
            if (!onlineKeys.isEmpty()) {
                for (String key : onlineKeys) {
                    OnlineUserStatusDto status = (OnlineUserStatusDto) redisTemplate.opsForValue().get(key);
                    if (status != null && sessionId.equals(status.getSessionId())) {
                        redisTemplate.delete(key);
                        logger.info("Removed online status for expired session: {}", sessionId);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi xóa online status theo sessionId: {}", e.getMessage(), e);
        }
    }

    private String extractSessionIdFromKey(String sessionKey) {
        // Extract session ID from spring:session:sessions:sessionId
        return sessionKey.substring(sessionKey.lastIndexOf(":") + 1);
    }
}
