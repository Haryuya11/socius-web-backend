package org.socius.sociuswebbackend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.SessionValidationService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnlineUserServiceImpl implements OnlineUserService {

    private static final Logger logger = LoggerFactory.getLogger(OnlineUserServiceImpl.class);

    final private RedisTemplate<String, Object> redisTemplate;
    final private UserRepository userRepository;
    final private ConfigService configService;
    final private SessionValidationService sessionValidationService;
    final private ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @Override
    public void updateUserOnlineStatus(UUID userId, String sessionId) {
        try {
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


            String key = RedisKeyBuilder.userOnlineKey(userId);
            int onlineStatusTimeout = configService.getInt("online.status.timeout.minutes", 2);

            redisTemplate.opsForValue().set(key, statusDto, Duration.ofMinutes(onlineStatusTimeout));
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
                int onlineStatusTimeout = configService.getInt("online.status.timeout.minutes", 2);
                redisTemplate.opsForValue().set(key, currentStatus, Duration.ofMinutes(onlineStatusTimeout));

                logger.info("Cập nhật heartbeat cho user: {} với session: {}", userId, currentStatus.getSessionId());
            } else {
                logger.warn("Không tìm thấy online status cho user: {} khi xử lý heartbeat", userId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý heartbeat: {}", e.getMessage(), e);
        }
    }

    @Override
    public void markUserOffline(UUID userId, String sessionId) {
        try {
            String key = RedisKeyBuilder.userOnlineKey(userId);
            OnlineUserStatusDto existingStatus = (OnlineUserStatusDto) redisTemplate.opsForValue().get(key);

            if (existingStatus != null && sessionId.equals(existingStatus.getSessionId())) {
                redisTemplate.delete(key);
                logger.info("Đánh dấu user {} offline với session {}", userId, sessionId);
            } else {
                logger.warn("Không tìm thấy online status cho user: {} hoặc session không khớp khi đánh dấu offline", userId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi đánh dấu user offline: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<OnlineUserStatusDto> getOnlineUsers() {
        try {
            String pattern = RedisKeyBuilder.getKeyPattern("user:") + ":online";
            Set<String> key = redisTemplate.keys(pattern);
            List<OnlineUserStatusDto> onlineUsers = new ArrayList<>();

            if (key.isEmpty()) {
                logger.info("Không tìm thấy người dùng online nào");
                return onlineUsers;
            }

            List<Object> values = redisTemplate.opsForValue().multiGet(key);
            if (values == null || values.isEmpty()) {
                logger.info("Không tìm thấy giá trị nào cho các khóa online user");
                return onlineUsers;
            }
            return values.stream()
                    .filter(Objects::nonNull)
                    .map(obj -> (OnlineUserStatusDto) obj)
                    .collect(Collectors.toList());
//            if (!key.isEmpty()) {
//                List<Object> values = redisTemplate.opsForValue().multiGet(key);
//                if (values != null) {
//                    for (Object value : values) {
//                        if (value instanceof OnlineUserStatusDto onlineUserStatusDto) {
//                            if (isRecentlyActive(onlineUserStatusDto.getLastSeen())) {
//                                onlineUsers.add(onlineUserStatusDto);
//                            }
//                        }
//                    }
//                }
//            }
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách người dùng online: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
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


    @Override
    public boolean isUserSessionValid(UUID userId) {
        // Delegate to SessionValidationService
        return sessionValidationService.hasValidSession(userId);
    }

    @Override
    public String getUserSessionId(UUID userId) {
        try {
            String key = RedisKeyBuilder.userOnlineKey(userId);
            OnlineUserStatusDto status = (OnlineUserStatusDto) redisTemplate.opsForValue().get(key);
            return status != null ? status.getSessionId() : null;
        } catch (Exception e) {
            logger.error("Error getting user session ID: {}", userId, e);
            return null;
        }
    }

    /**
     * Kiểm tra tính hợp lệ của session trong Redis
     *
     * @param sessionId ID của session cần kiểm tra
     * @return true nếu session hợp lệ, false nếu không
     */
    private boolean isSessionValid(String sessionId) {
        try {
            String sessionKey = RedisKeyBuilder.springSessionKey(sessionId);
            Boolean exists = redisTemplate.hasKey(sessionKey);
            Long expireTime = redisTemplate.getExpire(sessionKey);

            boolean isValid = exists != null && exists && expireTime != null && expireTime > 0;
            logger.debug("Session {} validation: exists={}, expireTime={}, valid={}",
                    sessionId, exists, expireTime, isValid);

            return isValid;
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra session {}: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }
}
