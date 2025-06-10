package org.socius.sociuswebbackend.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    final private ObjectMapper objectMapper;

    @Override
    public void storeOfflineMessage(UUID userId, MessageResponseDto message) {
        try {
            // ✅ Kiểm tra trùng lặp an toàn hơn
            if (checkIfMessageExists(userId, message)) {
                logger.debug("Message already exists for user: {}, messageId: {}", userId, message.getId());
                return;
            }

            String key = RedisKeyBuilder.chatOfflineKey(userId);
            String messageJson = objectMapper.writeValueAsString(message);

            redisTemplate.opsForList().rightPush(key, messageJson);
            redisTemplate.expire(key, Duration.ofDays(configService.getInt("offline.message.retention.days", 7)));

            // ✅ Log khác nhau cho system message và normal message
            if (message.getId() == null) {
                logger.info("Đã lưu system message offline cho người dùng {}: {}", userId, message.getContent());
            } else {
                logger.info("Đã lưu tin nhắn offline cho người dùng {}, messageId: {}", userId, message.getId());
            }
        } catch (Exception e) {
            logger.error("Error storing offline message for user: {}, messageId: {}",
                    userId, message.getId(), e);
        }
    }

    /**
     * Kiểm tra tin nhắn đã tồn tại trong danh sách offline chưa để tránh trùng lặp
     *
     * @param userId     ID của người dùng
     * @param messageDto Tin nhắn cần kiểm tra
     */
    private boolean checkIfMessageExists(UUID userId, MessageResponseDto messageDto) {
        try {
            // ✅ Kiểm tra null trước khi so sánh
            UUID messageId = messageDto.getId();
            if (messageId == null) {
                // Đối với tin nhắn hệ thống không có ID, luôn cho phép lưu
                logger.debug("Message has no ID (likely system message), allowing storage for user: {}", userId);
                return false;
            }

            String key = RedisKeyBuilder.chatOfflineKey(userId);
            List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);

            if (messages == null || messages.isEmpty()) {
                return false;
            }

            return messages.stream()
                    .map(obj -> {
                        try {
                            return objectMapper.readValue(obj.toString(), MessageResponseDto.class);
                        } catch (Exception e) {
                            logger.warn("Failed to parse stored message: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(msg -> msg != null && msg.getId() != null) // ✅ Thêm kiểm tra null
                    .anyMatch(msg -> messageId.equals(msg.getId()));
        } catch (Exception e) {
            logger.error("Error checking if message exists for user: {}, messageId: {}",
                    userId, messageDto.getId(), e);
            return false;
        }
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
