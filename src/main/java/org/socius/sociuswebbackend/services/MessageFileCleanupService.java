package org.socius.sociuswebbackend.services;

public interface MessageFileCleanupService {

    /**
     * Xóa các file đính kèm của tin nhắn đã bị đánh dấu xóa
     * Phương thức này sẽ được gọi định kỳ mỗi 0 giờ mỗi ngày
     */
    void cleanupDeletedMessagesFiles();
}
