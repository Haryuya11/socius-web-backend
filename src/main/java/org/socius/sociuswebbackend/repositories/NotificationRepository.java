package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    // Lấy danh sách thông báo theo userId của người nhận với phân trang
    @Query("SELECT n FROM NotificationEntity n JOIN n.recipients r WHERE r.user.id = :userId")
    Page<NotificationEntity> findByRecipientUserId(UUID userId, Pageable pageable);

    // Cập nhật trạng thái isRead và readAt cho một thông báo cụ thể của người dùng
    @Modifying
    @Query("UPDATE NotificationRecipientEntity r SET r.isRead = true, r.readAt = :readAt WHERE r.notification.id = :notificationId AND r.user.id = :userId")
    void markAsRead(UUID notificationId, UUID userId, LocalDateTime readAt);
}
