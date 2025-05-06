package org.socius.sociuswebbackend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class OnlineUserServiceImpl implements OnlineUserService {

    private static final Logger logger = LoggerFactory.getLogger(OnlineUserServiceImpl.class);

    private static final String ONLINE_USERS_PREFIX = "online:users:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigService configService;

    @Override
    public void updateUserOnlineStatus(UUID userId, String sessionId) {
        try {
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();

                OnlineUserStatusDto onlineUserStatusDto = OnlineUserStatusDto.builder()
                        .userId(userId)
                        .fullName(user.getFullName())
                        .imageUrl(user.getImageUrl())
                        .sessionId(sessionId)
                        .lastSeen(LocalDateTime.now())
                        .build();

                String redisKey = ONLINE_USERS_PREFIX + user.getId();
                redisTemplate.opsForValue().set(redisKey, onlineUserStatusDto);
                redisTemplate.expire(redisKey, 5, TimeUnit.MINUTES);

                logger.info("Cập nhật trạng thái online cho người dùng: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật trạng thái online: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleUserHeartbeat(UUID userId) {
        try {
            String redisKey = ONLINE_USERS_PREFIX + userId;
            OnlineUserStatusDto onlineUserStatusDto = (OnlineUserStatusDto) redisTemplate.opsForValue().get(redisKey);
            if (onlineUserStatusDto != null) {
                onlineUserStatusDto.setLastSeen(LocalDateTime.now());
                redisTemplate.opsForValue().set(redisKey, onlineUserStatusDto);
                redisTemplate.expire(redisKey, 5, TimeUnit.MINUTES);

                logger.info("Cập nhật heartbeat cho người dùng: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật heartbeat: {}", e.getMessage(), e);
        }

    }

    @Override
    public void markUserOffline(UUID userId, String sessionId) {
        try {
            String redisKey = ONLINE_USERS_PREFIX + userId;
            OnlineUserStatusDto onlineUserStatusDto = (OnlineUserStatusDto) redisTemplate.opsForValue().get(redisKey);

            if (onlineUserStatusDto != null && sessionId.equals(onlineUserStatusDto.getSessionId())) {
                redisTemplate.delete(redisKey);
                logger.info("Đánh dấu người dùng offline: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi đánh dấu người dùng offline: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<OnlineUserStatusDto> getOnlineUsers() {
        List<OnlineUserStatusDto> onlineUsers = new ArrayList<>();
        try {
            Set<String> redisKeys = redisTemplate.keys(ONLINE_USERS_PREFIX + "*");
            if (!redisKeys.isEmpty()) {
                List<Object> values = redisTemplate.opsForValue().multiGet(redisKeys);
                if (values != null) {
                    for (Object value : values) {
                        if (value instanceof OnlineUserStatusDto onlineUserStatusDto) {
                            if (isRecentlyActive(onlineUserStatusDto.getLastSeen())) {
                                onlineUsers.add(onlineUserStatusDto.toPublicDto());
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
            String redisKey = ONLINE_USERS_PREFIX + userId;
            OnlineUserStatusDto onlineUserStatusDto = (OnlineUserStatusDto) redisTemplate.opsForValue().get(redisKey);

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

}
