package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.ConnectionCleanupService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConnectionCleanupServiceImpl implements ConnectionCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionCleanupServiceImpl.class);
    final private OnlineUserService onlineUserService;
    final private ConfigService configService;
    final private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Scheduled(cron = "0 * * * * *")
    public void cleanupDeadConnections() {
        try {
            // Sử dụng distributed lock để tránh race condition
            String lockKey = "cleanup:dead-connections:lock";
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofMinutes(5));

            if (Boolean.FALSE.equals(acquired)) {
                logger.debug("Cleanup job đã được instance khác thực hiện");
                return;
            }

            try {
                performCleanup();
            } finally {
                redisTemplate.delete(lockKey);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi cleanup dead connections: {}", e.getMessage(), e);
        }
    }

    private void performCleanup() {
        long heartbeatTimeout = configService.getInt("websocket.heartbeat.timeout", 300000);
        String pattern = RedisKeyBuilder.userOnlinePattern();
        Set<String> keys = redisTemplate.keys(pattern);

        if (!keys.isEmpty()) { // Thêm null check
            int cleanedCount = 0;
            for (String key : keys) {
                try {
                    OnlineUserStatusDto status = (OnlineUserStatusDto) redisTemplate.opsForValue().get(key);
                    if (status != null) {
                        long timeSinceLastSeen = System.currentTimeMillis() -
                                status.getLastSeen().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                        if (timeSinceLastSeen > heartbeatTimeout) {
                            onlineUserService.markUserOffline(status.getUserId(), status.getSessionId());
                            cleanedCount++;
                            logger.info("Đã cleanup connection chết cho user {}", status.getUserId());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Lỗi khi xử lý cleanup cho key {}: {}", key, e.getMessage(), e);
                }
            }
            logger.info("Cleanup hoàn thành: {} connections đã được dọn dẹp", cleanedCount);
        }
    }
}
