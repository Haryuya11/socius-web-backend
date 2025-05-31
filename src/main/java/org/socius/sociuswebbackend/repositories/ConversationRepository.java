package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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


    @Query("""
                SELECT c FROM ConversationEntity c 
                WHERE c.type = 'DIRECT' 
                AND c.id IN (
                    SELECT m1.id.conversationId FROM ConversationMemberEntity m1 
                    WHERE m1.id.userId = :userId1 AND m1.leftAt IS NULL
                ) 
                AND c.id IN (
                    SELECT m2.id.conversationId FROM ConversationMemberEntity m2 
                    WHERE m2.id.userId = :userId2 AND m2.leftAt IS NULL
                )
            """)
    Optional<ConversationEntity> findDirectConversationBetweenUsers(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2
    );

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
     * Lấy tất cả cuộc trò chuyện mà user tham gia
     *
     * @param userId ID của người dùng
     * @return Danh sách cuộc trò chuyện
     */
    @Query("""
                SELECT DISTINCT c FROM ConversationEntity c 
                INNER JOIN c.members m 
                WHERE m.id.userId = :userId 
                AND m.leftAt IS NULL 
                ORDER BY c.updatedAt DESC
            """)
    List<ConversationEntity> findAllActiveConversationsByUserId(@Param("userId") UUID userId);


    /**
     * Lấy các cuộc trò chuyện đang hoạt động của người dùng với phân trang
     *
     * @param userId   ID của người dùng
     * @param pageable Thông tin phân trang
     * @return Trang các cuộc trò chuyện
     */
    @Query("""
                SELECT DISTINCT c FROM ConversationEntity c 
                INNER JOIN c.members m 
                WHERE m.id.userId = :userId 
                AND m.leftAt IS NULL 
                ORDER BY c.updatedAt DESC
            """)
    Page<ConversationEntity> findActiveConversationsByUserId(
            @Param("userId") UUID userId,
            Pageable pageable
    );
}
