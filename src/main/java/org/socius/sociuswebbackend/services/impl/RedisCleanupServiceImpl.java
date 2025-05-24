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

    private static final String ONLINE_PREFIX = "online:users:";
    private static final String RBAC_PREFIX = "rbac:";
    private static final String SESSION_PREFIX = "spring:session:";
    private static final String CACHE_PREFIX = "cache:";


    final private RedisTemplate<String, Object> redisTemplate;

    final private ConfigService configService;


    @Override
    @Scheduled(cron = "0 */5 * * * *")
    public void cleanupExpiredOnlineStatus() {
        try {
            Set<String> redisKeys = redisTemplate.keys(RedisKeyBuilder.getKeyPattern(ONLINE_PREFIX));
            if (!redisKeys.isEmpty()) {
                for (String redisKey : redisKeys) {
                    OnlineUserStatusDto onlineUserStatusDto = (OnlineUserStatusDto) redisTemplate.opsForValue().get(redisKey);
                    if (onlineUserStatusDto != null && !isRecentlyActive(onlineUserStatusDto.getLastSeen())) {
                        redisTemplate.delete(redisKey);
                        logger.info("Đã dọn dẹp trạng thái online hết hạn cho người dùng: {}", onlineUserStatusDto.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi dọn dẹp trạng thái online hết hạn: {}", e.getMessage(), e);
        }
    }

    @Override
    @Scheduled(cron = "0 */30 * * * *")
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
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredSession() {
        try {
            // Sử dụng pattern đúng cho expires keys
            String expiresPattern = RedisKeyBuilder.getSpringSessionExpiresPattern();
            Set<String> expiresKeys = redisTemplate.keys(expiresPattern);

            if (!expiresKeys.isEmpty()) {
                int count = 0;
                for (String expiresKey : expiresKeys) {
                    // Kiểm tra TTL của expires key
                    long ttl = redisTemplate.getExpire(expiresKey);

                    // Nếu key đã hết hạn (TTL <= 0 và khác -1 nghĩa là key không tồn tại)
                    if (ttl <= 0 && ttl != -1) {
                        // Extract sessionId từ expires key
                        String sessionId = RedisKeyBuilder.extractSessionIdFromExpiresKey(expiresKey);

                        // Tạo session key tương ứng
                        String sessionKey = RedisKeyBuilder.springSessionKey(sessionId);

                        // Xóa cả expires key và session key
                        redisTemplate.delete(expiresKey);
                        redisTemplate.delete(sessionKey);
                        count++;

                        logger.debug("Đã dọn dẹp phiên hết hạn: sessionId={}", sessionId);
                    }
                }
                if (count > 0) {
                    logger.info("Đã dọn dẹp {} phiên hết hạn", count);
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi dọn dẹp phiên hết hạn: {}", e.getMessage(), e);
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


    private boolean isRecentlyActive(LocalDateTime lastSeen) {
        if (lastSeen == null) return false;
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastSeen = Duration.between(lastSeen, now).toMinutes();

        int timeoutMinutes = configService.getInt("online.status.timeout.minutes", 5);

        return minutesSinceLastSeen < timeoutMinutes;
    }
}
