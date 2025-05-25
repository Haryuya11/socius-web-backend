package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.notification.NotificationRequestDto;
import org.socius.sociuswebbackend.model.dtos.notification.NotificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

/**
 * Interface định nghĩa các dịch vụ liên quan đến thông báo.
 */
public interface NotificationService {

    /**
     * Tạo một thông báo mới.
     *
     * @param requestDto DTO chứa nội dung thông báo và danh sách recipient IDs.
     * @return NotificationResponseDto chứa thông tin thông báo đã tạo.
     */
    NotificationResponseDto createNotification(NotificationRequestDto requestDto);

    /**
     * Lấy danh sách thông báo phân trang theo userId.
     *
     * @param userId ID của người dùng nhận thông báo.
     * @param pageable Thông tin phân trang (page, size).
     * @return Page<NotificationResponseDto> chứa danh sách thông báo phân trang.
     */
    Page<NotificationResponseDto> getNotificationsByUserId(UUID userId, Pageable pageable);

    /**
     * Đánh dấu thông báo là đã đọc cho người dùng cụ thể.
     *
     * @param notificationId ID của thông báo.
     * @param userId ID của người dùng.
     */
    void markNotificationAsRead(UUID notificationId, UUID userId);
}