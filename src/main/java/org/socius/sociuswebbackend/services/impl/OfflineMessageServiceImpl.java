package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.OfflineMessageService;
import org.socius.sociuswebbackend.util.RedisKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfflineMessageServiceImpl implements OfflineMessageService {

    private static final Logger logger = LoggerFactory.getLogger(OfflineMessageServiceImpl.class);

    final private RedisTemplate<String, Object> redisTemplate;
    final private ConfigService configService;

    @Override
    public void storeOfflineMessage(UUID userId, MessageResponseDto message) {
        String key = RedisKeyBuilder.chatOfflineKey(userId);

        // Kiểm tra nếu tin nhắn này đã tồn tại trong danh sách tin nhắn offline
        boolean exists = checkIfMessageExists(key, message);
        if (exists) {
            logger.info("Tin nhắn đã tồn tại trong danh sách offline cho người dùng {}", userId);
            return;
        }

        redisTemplate.opsForList().rightPush(key, message);

        // Đặt TTL cho key
        if (!redisTemplate.hasKey(key)) {
            int expiryDays = configService.getInt("chat.offline.messages.expiry.days", 7);
            redisTemplate.expire(key, Duration.ofDays(expiryDays));
        }

        // Kiểm tra và giới hạn số lượng tin nhắn, tránh quá tải
        Long size = redisTemplate.opsForList().size(key);
        int maxOfflineMessages = configService.getInt("chat.offline.messages.max", 1000);
        if (size != null && size > maxOfflineMessages) {
            // Loại bỏ tin nhắn cũ nếu vượt quá giới hạn
            redisTemplate.opsForList().trim(key, size - maxOfflineMessages, -1);
            logger.info("Giới hạn tin nhắn offline cho người dùng {} xuống {} tin nhắn", userId, maxOfflineMessages);
        }

        logger.info("Đã lưu tin nhắn offline cho người dùng {}, messageId: {}", userId, message.getId());
    }

    /**
     * Kiểm tra tin nhắn đã tồn tại trong danh sách offline chưa để tránh trùng lặp
     *
     * @param key        Key của danh sách tin nhắn
     * @param newMessage Tin nhắn mới
     */
    private boolean checkIfMessageExists(String key, MessageResponseDto newMessage) {
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return false;
        }

        List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
        if (messages != null) {
            for (Object obj : messages) {
                if (obj instanceof MessageResponseDto message) {
                    if (message.getId().equals(newMessage.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<MessageResponseDto> getOfflineMessages(UUID userId) {
        String key = RedisKeyBuilder.chatOfflineKey(userId);
        Long size = redisTemplate.opsForList().size(key);
        if (size == null || size == 0) {
            return new ArrayList<>();
        }

        List<Object> objects = redisTemplate.opsForList().range(key, 0, size - 1);
        List<MessageResponseDto> result = new ArrayList<>();

        if (objects != null) {
            for (Object object : objects) {
                if (object instanceof MessageResponseDto) {
                    result.add((MessageResponseDto) object);
                }
            }
        }

        logger.info("Đã lấy {} tin nhắn offline cho người dùng {}", result.size(), userId);
        return result;
    }

    @Override
    public void clearOfflineMessages(UUID userId) {
        String key = RedisKeyBuilder.chatOfflineKey(userId);
        redisTemplate.delete(key);
        logger.info("Đã xóa tin nhắn offline cho người dùng {}", userId);
    }
}
