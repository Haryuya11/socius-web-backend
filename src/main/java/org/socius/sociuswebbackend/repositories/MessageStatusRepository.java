package org.socius.sociuswebbackend.repositories;

import jakarta.transaction.Transactional;
import org.socius.sociuswebbackend.model.entities.MessageStatusEntity;
import org.socius.sociuswebbackend.model.entities.MessageStatusId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MessageStatusRepository extends JpaRepository<MessageStatusEntity, MessageStatusId> {

    /**
     * Tìm kiếm tất cả trạng thái tin nhắn của một người dùng trong một cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param userId         ID của người dùng
     * @return Danh sách trạng thái tin nhắn
     */
    @Query("SELECT ms FROM MessageStatusEntity ms WHERE ms.message.conversation.id = :conversationId AND ms.user.id = :userId AND ms.isRead = false")
    List<MessageStatusEntity> findUnreadMessageStatusByConversationAndUser(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId);

    /**
     * Cập nhật trạng thái tin nhắn là đã đọc
     *
     * @param conversationId    ID của cuộc trò chuyện
     * @param userId            ID của người dùng
     * @param lastReadMessageId ID của tin nhắn cuối cùng đã đọc
     * @param readTime          Thời gian đọc tin nhắn
     * @return Số lượng bản ghi đã được cập nhật
     */
    @Modifying
    @Query("UPDATE MessageStatusEntity ms SET ms.isRead = true, ms.readAt = :readTime WHERE " +
            "ms.message.conversation.id = :conversationId AND ms.user.id = :userId AND " +
            "ms.message.id <= :lastReadMessageId AND ms.isRead = false")
    @Transactional
    int markMessagesAsRead(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("lastReadMessageId") UUID lastReadMessageId,
            @Param("readTime") LocalDateTime readTime);
}