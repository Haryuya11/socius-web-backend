package org.socius.sociuswebbackend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.model.dtos.user.OnlineUserStatusDto;
import org.socius.sociuswebbackend.model.entities.UserEntity;
import org.socius.sociuswebbackend.repositories.UserRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.OfflineMessageService;
import org.socius.sociuswebbackend.services.OnlineUserService;
import org.socius.sociuswebbackend.services.PendingMessagesService;
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
    final private OfflineMessageService offlineMessageService;
    final private PendingMessagesService pendingMessagesService;
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

            List<MessageResponseDto> offlineMessages = offlineMessageService.getOfflineMessages(userId);
            if (!offlineMessages.isEmpty()) {
                logger.info("Sending {} offline messages to user {}", offlineMessages.size(), userId);
                // OfflineMessageService sẽ tự động gửi qua WebSocket
            }

            // Gửi pending messages
            List<MessageResponseDto> pendingMessages = pendingMessagesService.getPendingMessages(userId);
            if (!pendingMessages.isEmpty()) {
                logger.info("Sent {} pending messages to user {}", pendingMessages.size(), userId);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi cập nhật online status: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleUserHeartbeat(UUID userId) {
        try {
            String userOnlineKey = RedisKeyBuilder.userOnlineKey(userId);

            // Chỉ refresh TTL nếu key tồn tại
            if (redisTemplate.hasKey(userOnlineKey)) {
                int onlineStatusTtl = configService.getInt("online.status.timeout.minutes", 2);
                redisTemplate.expire(userOnlineKey, Duration.ofMinutes(onlineStatusTtl));

                logger.debug("Refreshed online status TTL for user: {}", userId);
            }
        } catch (Exception e) {
            logger.warn("Error handling heartbeat for user {}: {}", userId, e.getMessage());
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
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách người dùng online: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<OnlineUserStatusDto> getOnlineUsersWithExceptSelf() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                logger.warn("Không có người dùng đăng nhập để lấy danh sách online ngoại trừ bản thân");
                return new ArrayList<>();
            }
            String userEmail = auth.getName();
            UserEntity currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại: " + userEmail));

            UUID currentUserId = currentUser.getId();
            String pattern = RedisKeyBuilder.getKeyPattern("user:") + ":online";
            Set<String> keys = redisTemplate.keys(pattern);
            List<OnlineUserStatusDto> onlineUsers = new ArrayList<>();
            if (keys.isEmpty()) {
                logger.info("Không tìm thấy người dùng online nào ngoại trừ bản thân");
                return onlineUsers;
            }

            List<Object> values = redisTemplate.opsForValue().multiGet(keys);
            if (values == null || values.isEmpty()) {
                logger.info("Không tìm thấy giá trị nào cho các khóa online user ngoại trừ bản thân");
                return onlineUsers;
            }

            onlineUsers = values.stream()
                    .filter(Objects::nonNull)
                    .map(obj -> (OnlineUserStatusDto) obj)
                    .filter(user -> !user.getUserId().equals(currentUserId)) // Loại trừ người dùng hiện tại
                    .collect(Collectors.toList());

            return onlineUsers;


        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách người dùng online ngoại trừ bản thân: {}", e.getMessage(), e);
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
}
