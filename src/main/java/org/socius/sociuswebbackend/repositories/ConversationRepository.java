package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.ConversationEntity;
import org.socius.sociuswebbackend.model.enums.ConversationType;
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
                JOIN c.members m1
                JOIN c.members m2
                WHERE c.type = 'DIRECT'   
                AND m1.user.id = :userId1
                AND m2.user.id = :userId2
                AND m1.user.id != m2.user.id
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

    /**
     * Tìm cuộc trò chuyện theo tên và type
     *
     * @param groupChatName    Tên cuộc trò chuyện nhóm
     * @param conversationType Loại cuộc trò chuyện
     * @return Optional chứa cuộc trò chuyện nếu tìm thấy, empty nếu không tìm thấy
     */
    Optional<ConversationEntity> findByNameAndType(String groupChatName, ConversationType conversationType);
}
