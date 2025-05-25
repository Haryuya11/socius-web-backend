package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.PendingMessagesService;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PendingMessagesServiceImpl implements PendingMessagesService {

    private static final Logger logger = LoggerFactory.getLogger(PendingMessagesServiceImpl.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final ConfigService configService;


    @Override
    public void initializeBuffer(UUID userId) {
        String key = RabbitMQKeyBuilder.getPendingMessagesKey(userId);
        // Kiểm tra xem buffer đã tồn tại chưa
        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForList().rightPushAll(key, new ArrayList<>());

            int expiryMinutes = configService.getInt("websocket.pending.messages.expiry.minutes", 30);
            redisTemplate.expire(key, Duration.ofMinutes(expiryMinutes));
            logger.info("Khởi tạo buffer cho người dùng: {}", userId);

            // Liên kết buffer với phiên hiện tại
            String sessionId = getCurrentSessionId(userId);
            if (sessionId != null) {
                redisTemplate.opsForValue().set(RedisKeyBuilder.pendingSessionKey(userId), sessionId,
                        Duration.ofMinutes(expiryMinutes));
            }
        } else {
            logger.info("Buffer đã tồn tại cho người dùng: {}", userId);
        }
    }


    @Override
    public void addPendingMessage(UUID userId, MessageResponseDto message) {
        String key = RabbitMQKeyBuilder.getPendingMessagesKey(userId);
        // Thêm tin nhắn vào buffer
        redisTemplate.opsForList().rightPush(key, message);
        logger.info("Thêm tin nhắn vào buffer cho người dùng: {}", userId);
    }

    @Override
    public List<MessageResponseDto> getPendingMessages(UUID userId) {
        String key = RabbitMQKeyBuilder.getPendingMessagesKey(userId);
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return new ArrayList<>();
        }

        // Lấy danh sách tin nhắn từ buffer
        List<Object> objects = redisTemplate.opsForList().range(key, 0, -1);
        List<MessageResponseDto> messages = new ArrayList<>();

        if (objects != null) {
            for (Object object : objects) {
                if (object instanceof MessageResponseDto) {
                    messages.add((MessageResponseDto) object);
                }
            }
        }

        logger.info("Lấy {} tin nhắn từ buffer cho người dùng: {}", messages.size(), userId);
        return messages;
    }

    @Override
    public void clearBuffer(UUID userId) {
        String key = RabbitMQKeyBuilder.getPendingMessagesKey(userId);
        redisTemplate.delete(key);
        logger.info("Xóa buffer cho người dùng: {}", userId);
    }

    /**
     * Lấy ID phiên hiện tại của người dùng
     *
     * @param userId ID người dùng
     */
    private String getCurrentSessionId(UUID userId) {
        return (String) redisTemplate.opsForValue().get(RedisKeyBuilder.userCurrentSessionKey(userId));
    }
}
