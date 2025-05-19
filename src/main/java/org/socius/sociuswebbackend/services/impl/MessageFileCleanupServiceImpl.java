package org.socius.sociuswebbackend.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.socius.sociuswebbackend.repositories.MessageRepository;
import org.socius.sociuswebbackend.services.ConfigService;
import org.socius.sociuswebbackend.services.FileStorageService;
import org.socius.sociuswebbackend.services.MessageFileCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageFileCleanupServiceImpl implements MessageFileCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(MessageFileCleanupServiceImpl.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    @Scheduled(cron = "0 0 0 * * *") // Chạy hàng ngày lúc 00:00
    @Transactional
    public void cleanupDeletedMessagesFiles() {
        logger.info("Bắt đầu quá trình dọn dẹp file của tin nhắn đã xóa");

        // Tìm tất cả tin nhắn đã bị xóa có file đính kèm và chưa bị xóa file
        List<MessageEntity> deletedMessages = messageRepository.findDeletedMessagesWithMedia();

        int successCount = 0;
        int failureCount = 0;

        for (MessageEntity message : deletedMessages){
            try {
                // Xóa file đính kèm
                if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
                    fileStorageService.deleteFile(message.getMediaUrl());
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
}
