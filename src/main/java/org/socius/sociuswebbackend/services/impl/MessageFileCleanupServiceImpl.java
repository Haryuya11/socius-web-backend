package org.socius.sociuswebbackend.services.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.socius.sociuswebbackend.repositories.MessageRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.FileStorageService;
import org.socius.sociuswebbackend.services.MessageFileCleanupService;
import org.socius.sociuswebbackend.util.RabbitMQKeyBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MessageFileCleanupServiceImpl implements MessageFileCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(MessageFileCleanupServiceImpl.class);

    final private MessageRepository messageRepository;
    final private FileStorageService fileStorageService;
    final private ConfigService configService;
    final private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupDeletedMessagesFiles() {
        logger.info("Bắt đầu quá trình dọn dẹp file của tin nhắn đã xóa");

        // Số ngày dữ lại file sau khi tin nhắn bị xóa
        int retentionDays = configService.getInt("message.file.cleanup.days", 30);

        // Tìm tất cả tin nhắn đã bị xóa có file đính kèm và chưa bị xóa file
        List<MessageEntity> deletedMessages = messageRepository.findDeletedMessagesForCleanup(retentionDays);

        int successCount = 0;
        int failureCount = 0;

        for (MessageEntity message : deletedMessages) {
            try {
                // Xóa file đính kèm
                if (message.getFileUrl() != null && !message.getFileUrl().isEmpty()) {
                    fileStorageService.deleteFile(message.getFileUrl());
                    message.setMediaCleanedUp(true);
                    messageRepository.save(message);
                    successCount++;
                }
            } catch (Exception e) {
                logger.error("Lỗi khi xóa file của tin nhắn {}: {}", message.getId(), e.getMessage());
                failureCount++;
            }
        }
        logger.info("Quá trình dọn dẹp file hoàn tất: {} thành công, {} thất bại", successCount, failureCount);
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Override
    @Transactional
    public void cleanupOrphanedFiles() {
        logger.info("Bắt đầu quá trình dọn dẹp file không còn liên kết");

        // Tìm tất cả file không còn liên kết
        List<String> orphanedFiles = messageRepository.findOrphanedFiles();

        int successCount = 0;
        int failureCount = 0;

        for (String filePath : orphanedFiles) {
            try {
                // Xóa file không còn liên kết
                fileStorageService.deleteFile(filePath);
                successCount++;
            } catch (Exception e) {
                logger.error("Lỗi khi xóa file không còn liên kết {}: {}", filePath, e.getMessage());
                failureCount++;
            }
        }
        logger.info("Quá trình dọn dẹp file không còn liên kết hoàn tất: {} thành công, {} thất bại", successCount, failureCount);
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredPendingMessages() {
        try {
            logger.info("Bắt đầu quá trình dọn dẹp tin nhắn đã hết hạn");

            String pattern = RabbitMQKeyBuilder.getPendingMessagesPattern();
            Set<String> keys = redisTemplate.keys(pattern);
            int deletedCount = 0;
            if (!keys.isEmpty()) {
                for (String key : keys) {
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl <= 0) {
                        // Nếu TTL <= 0, xóa tin nhắn hết hạn
                        redisTemplate.delete(key);
                        deletedCount++;
                        logger.info("Đã xóa pending messages hết hạn: {}", key);
                    }
                }
            }

            logger.info("Quá trình dọn dẹp tin nhắn đã hết hạn hoàn tất: {} tin nhắn đã bị xóa", deletedCount);
        } catch (Exception e) {
            logger.error("Lỗi khi dọn dẹp tin nhắn đã hết hạn: {}", e.getMessage(), e);
        }
    }
}
