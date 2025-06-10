package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;

import java.util.List;
import java.util.UUID;

public interface OfflineMessageService {

    /**
     * Lưu tin nhắn cho người dùng offline
     *
     * @param userId  ID của người dùng nhận tin nhắn
     * @param message Tin nhắn cần lưu
     */
    void storeOfflineMessage(UUID userId, MessageResponseDto message);

    /**
     * Lấy tất cả tin nhắn offline của người dùng
     *
     * @param userId ID người dùng
     * @return Danh sách tin nhắn
     */
    List<MessageResponseDto> getOfflineMessages(UUID userId);

    /**
     * Xóa tin nhắn offline sau khi đã được gửi
     *
     * @param userId ID người dùng
     */
    void clearOfflineMessages(UUID userId);
}
