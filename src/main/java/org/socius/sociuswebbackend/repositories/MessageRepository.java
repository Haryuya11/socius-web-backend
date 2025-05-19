package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

    /**
     * Tìm kiếm các tin nhắn trong một cuộc trò chuyện theo conversationId và sắp xếp theo thời gian tạo giảm dần.
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param pageable       Thông tin phân trang
     * @return Danh sách các tin nhắn trong cuộc trò chuyện
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.conversation.id = :conversationId ORDER BY m.createdAt DESC")
    Page<MessageEntity> findByConversationIdOrderByCreatedAtDesc(
            @Param("conversationId") UUID conversationId, Pageable pageable);

    /**
     * Tìm kiếm các tin nhắn mới hơn một tin nhắn cụ thể trong một cuộc trò chuyện.
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param lastMessageId  ID của tin nhắn cuối cùng đã đọc
     * @return Danh sách các tin nhắn mới hơn
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.conversation.id = :conversationId AND m.id > :lastMessageId ORDER BY m.createdAt ASC")
    List<MessageEntity> findNewerMessages(
            @Param("conversationId") UUID conversationId,
            @Param("lastMessageId") UUID lastMessageId);

    /**
     * Tìm kiếm các tin nhắn mới nhất trong một cuộc trò chuyện.
     *
     * @param conversationIds Danh sách ID của các cuộc trò chuyện
     * @return Danh sách các tin nhắn mới nhất trong các cuộc trò chuyện
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.conversation.id IN :conversationIds AND m.id = " +
            "(SELECT MAX(m2.id) FROM MessageEntity m2 WHERE m2.conversation.id = m.conversation.id)")
    <T>
    List<MessageEntity> findLatestMessagesForConversations(@Param("conversationIds") List<UUID> conversationIds);

    /**
     * Tìm kiếm các tin nhắn đã bị xóa có file đính kèm và chưa được dọn dẹp
     *
     * @return Danh sách các tin nhắn cần xóa file
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.isDeleted = true AND m.mediaUrl IS NOT NULL AND m.mediaUrl != '' AND m.mediaCleanedUp = false")
    List<MessageEntity> findDeletedMessagesWithMedia();

    /**
     * Tìm kiếm các tin nhắn trong một cuộc trò chuyện theo từ khóa.
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param keyword        Từ khóa tìm kiếm
     * @param pageable       Thông tin phân trang
     * @return Trang các tin nhắn tìm thấy
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.conversation.id = :conversationId AND " +
            "LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) AND m.isDeleted = false " +
            "ORDER BY m.createdAt DESC")
    Page<MessageEntity> searchMessages(
            @Param("conversationId") UUID conversationId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
