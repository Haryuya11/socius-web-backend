package org.socius.sociuswebbackend.services;

public interface RedisCleanupService {

    /**
     *  Xóa trạng thái online của người dùng đã hết hạn mỗi 5 phút
     */
    void cleanupExpiredOnlineStatus();

    /**
     *  Dọn dẹp thông tin quyền người dùng đã hết hạn mỗi 30 phút
     */
    void cleanupExpiredUserPermissions();

    /**
     * Dọn dẹp phiên làm việc đã hết hạn mỗi 60 phút
     */
    void cleanupExpiredSession();

    /**
     * Dọn dẹp cache đã hết hạn mỗi 60 phút
     */
    void cleanupExpiredCache();
}
