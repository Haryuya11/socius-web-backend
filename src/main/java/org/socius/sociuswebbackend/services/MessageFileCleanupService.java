package org.socius.sociuswebbackend.services;

public interface MessageFileCleanupService {

    /**
     * Xóa các file đính kèm của tin nhắn đã bị đánh dấu xóa
     * Phương thức này sẽ được gọi định kỳ mỗi 0 giờ mỗi ngày
     */
    void cleanupDeletedMessagesFiles();

    /**
     * Xóa các file đính kèm mồ côi
     * Phương thức này sẽ được gọi định kỳ mỗi 1 giờ sáng
     */
    void cleanupOrphanedFiles();

    /**
     * Dọn dẹp các tin nhắn đã hết hạn và không còn cần thiết
     * Phương thức này sẽ được gọi định kỳ mỗi 1 giờ
     */
    void cleanupExpiredPendingMessages();
}
