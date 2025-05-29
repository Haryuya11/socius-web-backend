package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {

    /**
     * Tìm kiếm các cuộc trò chuyện của người dùng theo ID người dùng
     *
     * @param userId   ID của người dùng
     * @param pageable Thông tin phân trang
     * @return Danh sách các cuộc trò chuyện của người dùng
     */

    @Query("SELECT c FROM ConversationEntity c JOIN c.members m WHERE m.user.id = :userId ORDER BY c.updatedAt DESC")
    Page<ConversationEntity> findConversationsByUserId(@Param("userId") UUID userId, Pageable pageable);


    /**
     * Tìm kiếm cuộc trò chuyện trực tiếp giữa hai người dùng
     *
     * @param userId1 ID của người dùng 1
     * @param userId2 ID của người dùng 2
     * @return Cuộc trò chuyện trực tiếp giữa hai người dùng, nếu có
     */
    @Query("SELECT c FROM ConversationEntity c WHERE c.type = 'DIRECT' AND " +
            "EXISTS (SELECT m1 FROM ConversationMemberEntity m1 WHERE m1.conversation = c AND m1.user.id = :userId1) AND " +
            "EXISTS (SELECT m2 FROM ConversationMemberEntity m2 WHERE m2.conversation = c AND m2.user.id = :userId2)")
    Optional<ConversationEntity> findDirectConversationBetweenUsers(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2);

    /**
     * Tìm kiếm các cuộc trò chuyện của người dùng theo ID người dùng
     *
     * @param userId   ID của người dùng
     * @param pageable Thông tin phân trang
     * @return Danh sách các cuộc trò chuyện của người dùng
     */
    @Query("SELECT c FROM ConversationEntity c JOIN c.members m WHERE m.user.id = :userId ORDER BY c.updatedAt DESC")
    Page<ConversationEntity> findUserConversations(UUID userId, Pageable pageable);

    /**
     * Tìm kiếm các cuộc trò chuyện mà user là thành viên active
     */
    @Query("SELECT DISTINCT c FROM ConversationEntity c " +
            "JOIN ConversationMemberEntity cm ON c.id = cm.id.conversationId " +
            "WHERE cm.id.userId = :userId AND cm.leftAt IS NULL " +
            "ORDER BY c.updatedAt DESC")
    Page<ConversationEntity> findActiveConversationsByUserId(@Param("userId") UUID userId, Pageable pageable);
}
