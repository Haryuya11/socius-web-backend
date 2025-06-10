package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.UnreadCountEntity;
import org.socius.sociuswebbackend.model.entities.UnreadCountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnreadCountRepository extends JpaRepository<UnreadCountEntity, UnreadCountId> {

    /**
     * Tìm kiếm số lượng tin nhắn chưa đọc của người dùng trong một cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param userId         ID của người dùng
     * @return Optional chứa số lượng tin nhắn chưa đọc, nếu không tìm thấy thì trả về Optional.empty()
     */
    Optional<UnreadCountEntity> findByIdConversationIdAndIdUserId(UUID conversationId, UUID userId);

    /**
     * Tìm kiếm số lượng tin nhắn chưa đọc của người dùng
     *
     * @param userId ID của người dùng
     * @return Số lượng tin nhắn chưa đọc
     */
    @Query("SELECT uc FROM UnreadCountEntity uc WHERE uc.user.id = :userId")
    List<UnreadCountEntity> findByUserId(@Param("userId") UUID userId);

    /**
     * Cập nhật số lượng tin nhắn chưa đọc và tin nhắn cuối cùng đã đọc
     *
     * @param conversationId    ID của cuộc trò chuyện
     * @param userId            ID của người dùng
     * @param count             Số lượng tin nhắn chưa đọc mới
     * @param lastReadMessageId ID của tin nhắn cuối cùng đã đọc
     */
    @Modifying
    @Query(value = "INSERT INTO unread_counts (conversation_id, user_id, unread_count, last_read_message_id) " +
            "VALUES (:conversationId, :userId, :count, :lastReadMessageId) " +
            "ON CONFLICT (conversation_id, user_id) " +
            "DO UPDATE SET unread_count = :count, last_read_message_id = :lastReadMessageId",
            nativeQuery = true)
    @Transactional
    void updateUnreadCount(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId,
            @Param("count") int count,
            @Param("lastReadMessageId") UUID lastReadMessageId);

    /**
     * Tăng số lượng tin nhắn chưa đọc cho một danh sách người dùng trong một cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param userIds        Danh sách ID của người dùng
     * @return Số lượng bản ghi đã được cập nhật
     */
    @Modifying
    @Query("UPDATE UnreadCountEntity uc SET uc.unreadCount = uc.unreadCount + 1 " +
            "WHERE uc.conversation.id = :conversationId AND uc.user.id IN :userIds")
    int incrementUnreadCountForUsers(
            @Param("conversationId") UUID conversationId,
            @Param("userIds") List<UUID> userIds);

    /**
     * Tăng số lượng tin nhắn chưa đọc lên 1
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param userId         ID của người dùng
     */
    @Modifying
    @Query(value = "INSERT INTO unread_counts (conversation_id, user_id, unread_count) " +
            "VALUES (:conversationId, :userId, 1) " +
            "ON CONFLICT (conversation_id, user_id) " +
            "DO UPDATE SET unread_count = unread_counts.unread_count + 1",
            nativeQuery = true)
    @Transactional
    void incrementUnreadCount(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

    void deleteByConversation_Id(UUID conversationId);
}