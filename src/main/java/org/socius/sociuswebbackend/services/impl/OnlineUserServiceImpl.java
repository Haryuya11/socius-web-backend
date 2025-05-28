package org.socius.sociuswebbackend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    private static final Logger logger = LoggerFactory.getLogger(OnlineUserServiceImpl.class);

    final private RedisTemplate<String, Object> redisTemplate;
    final private UserRepository userRepository;
    final private ConfigService configService;

    @Override
    public void updateUserOnlineStatus(UUID userId, String sessionId) {
        try {
            String key = RedisKeyBuilder.userOnlineKey(userId);

            // Lấy thông tin user
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                logger.warn("User not found: {}", userId);
                return;
            }

            UserEntity user = userOpt.get();
            OnlineUserStatusDto statusDto = OnlineUserStatusDto.builder()
                    .userId(userId)
                    .fullName(user.getFullName())
                    .imageUrl(user.getImageUrl())
                    .sessionId(sessionId)
                    .lastSeen(LocalDateTime.now())
                    .isOnline(true)
                    .build();

            int onlineStatusTimeout = configService.getInt("online.status.timeout.minutes", 5);

            redisTemplate.opsForValue().set(key, statusDto);
            redisTemplate.expire(key, Duration.ofMinutes(onlineStatusTimeout));

            logger.debug("Cập nhật online status cho user: {} với session: {}", userId, sessionId);

        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật online status: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleUserHeartbeat(UUID userId) {
        try {
            String key = RedisKeyBuilder.userOnlineKey(userId);
            OnlineUserStatusDto currentStatus = (OnlineUserStatusDto) redisTemplate.opsForValue().get(key);

            if (currentStatus != null) {
                // Chỉ cập nhật lastSeen, GIỮ NGUYÊN TTL
                currentStatus.setLastSeen(LocalDateTime.now());
                currentStatus.setOnline(true);

                // Kiểm tra session còn hợp lệ không
                if (isSessionValid(currentStatus.getSessionId())) {
                    // Session còn hợp lệ -> cập nhật heartbeat
                    redisTemplate.opsForValue().set(key, currentStatus);
                    logger.debug("Updated heartbeat for user: {}", userId);
                } else {
                    // Session đã hết hạn -> xóa online status
                    redisTemplate.delete(key);
                    logger.info("Removed online status for user {} due to expired session", userId);
                }
            } else {
                logger.debug("No online status found for user: {} during heartbeat", userId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý heartbeat: {}", e.getMessage(), e);
        }
    }

    @Override
    public void markUserOffline(UUID userId, String sessionId) {
        try {
            String key = RedisKeyBuilder.userOnlineKey(userId);
            Object value = redisTemplate.opsForValue().get(key);

            if (value != null) {
                OnlineUserStatusDto currentStatus = convertToDto(value);

                // CHỈ xóa online status nếu sessionId khớp
                if (currentStatus != null && sessionId.equals(currentStatus.getSessionId())) {
                    redisTemplate.delete(key);
                    logger.info("Đánh dấu user {} offline (session: {})", userId, sessionId);
                } else {
                    logger.debug("Không xóa online status - sessionId không khớp cho user: {}", userId);
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi đánh dấu user offline: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<OnlineUserStatusDto> getOnlineUsers() {
        List<OnlineUserStatusDto> onlineUsers = new ArrayList<>();
        try {
            String pattern = RedisKeyBuilder.getKeyPattern("user:") + ":online";
            Set<String> key = redisTemplate.keys(pattern);
            if (!key.isEmpty()) {
                List<Object> values = redisTemplate.opsForValue().multiGet(key);
                if (values != null) {
                    for (Object value : values) {
                        if (value instanceof OnlineUserStatusDto onlineUserStatusDto) {
                            if (isRecentlyActive(onlineUserStatusDto.getLastSeen())) {
                                onlineUsers.add(onlineUserStatusDto);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách người dùng online: {}", e.getMessage(), e);
        }
        return onlineUsers;
    }

    @Override
    public boolean isUserOnline(UUID userId) {
        try {
            String key = RedisKeyBuilder.userOnlineKey(userId);
            Object value = redisTemplate.opsForValue().get(key);

            OnlineUserStatusDto onlineUserStatusDto;
            if (value instanceof OnlineUserStatusDto) {
                onlineUserStatusDto = (OnlineUserStatusDto) value;
            } else if (value instanceof Map) {
                // Chuyển đổi từ Map sang DTO thông qua ObjectMapper
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                onlineUserStatusDto = mapper.convertValue(value, OnlineUserStatusDto.class);
            } else {
                return false;
            }
            return onlineUserStatusDto != null && isRecentlyActive(onlineUserStatusDto.getLastSeen());
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra trạng thái người dùng: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean isRecentlyActive(LocalDateTime lastSeen) {
        if (lastSeen == null) return false;
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastSeen = Duration.between(lastSeen, now).toMinutes();

        int timeoutMinutes = configService.getInt("online.status.timeout.minutes", 5);

        return minutesSinceLastSeen < timeoutMinutes;
    }

    private OnlineUserStatusDto convertToDto(Object value) {
        if (value instanceof OnlineUserStatusDto) {
            return (OnlineUserStatusDto) value;
        } else if (value instanceof Map) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                return mapper.convertValue(value, OnlineUserStatusDto.class);
            } catch (Exception e) {
                logger.error("Lỗi chuyển đổi Map sang DTO: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    private boolean isSessionValid(String sessionId) {
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
}
