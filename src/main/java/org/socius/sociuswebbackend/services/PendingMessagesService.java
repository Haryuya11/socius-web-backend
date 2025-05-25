package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.message.MessageResponseDto;

import java.util.List;
import java.util.UUID;

public interface PendingMessagesService {

    /**
     * Khởi tạo buffer để lưu trữ tin nhắn cho người dùng mất kết nối
     *
     * @param userId ID của người dùng
     */
    void initializeBuffer(UUID userId);

    /**
     * Thêm tin nhắn vào buffer của người dùng
     *
     * @param userId  ID của người dùng
     * @param message Tin nhắn cần thêm vào buffer
     */
    void addPendingMessage(UUID userId, MessageResponseDto message);

    /**
     * Lấy danh sách tin nhắn đang chờ cho người dùng
     *
     * @param userId ID của người dùng
     * @return Danh sách tin nhắn đang chờ
     */
    List<MessageResponseDto> getPendingMessages(UUID userId);

    /**
     * Xóa buffer của người dùng
     *
     * @param userId ID của người dùng
     */
    void clearBuffer(UUID userId);
}
