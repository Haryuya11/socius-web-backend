package org.socius.sociuswebbackend.repositories;

import org.socius.sociuswebbackend.model.entities.ConversationMemberEntity;
import org.socius.sociuswebbackend.model.entities.ConversationMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ConversationMemberRepository extends JpaRepository<ConversationMemberEntity, ConversationMemberId> {

    /**
     * Tìm kiếm thành viên trong một cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @return Danh sách các thành viên trong cuộc trò chuyện
     */
    @Query("SELECT cm FROM ConversationMemberEntity cm WHERE cm.conversation.id = :conversationId AND cm.leftAt IS NULL")
    List<ConversationMemberEntity> findActiveMembers(@Param("conversationId") UUID conversationId);

    /**
     * Tìm kiếm Id thành viên trong một cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @return Danh sách các Id thành viên trong cuộc trò chuyện
     */
    @Query("SELECT cm.user.id FROM ConversationMemberEntity cm WHERE cm.conversation.id = :conversationId AND cm.leftAt IS NULL")
    Set<UUID> findActiveMemberIds(@Param("conversationId") UUID conversationId);

    /**
     * Kiểm tra xem một thành viên có phải là thành viên của một cuộc trò chuyện hay không
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param userId         ID của người dùng
     * @return true nếu thành viên đã tham gia cuộc trò chuyện, false nếu không
     */
    boolean existsByIdConversationIdAndIdUserIdAndLeftAtIsNull(UUID conversationId, UUID userId);

    /**
     * Xóa cuộc trò chuyện theo ID cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     */
    void deleteByConversation_Id(UUID conversationId);

    /**
     * Tìm kiếm tất cả thành viên (bao gồm cả những người đã rời)
     */
    @Query("SELECT cm FROM ConversationMemberEntity cm WHERE cm.id.conversationId = :conversationId")
    List<ConversationMemberEntity> findAllMembers(@Param("conversationId") UUID conversationId);

    /**
     * Kiểm tra user có phải thành viên active không
     */
    @Query("SELECT cm FROM ConversationMemberEntity cm WHERE cm.id.conversationId = :conversationId AND cm.id.userId = :userId AND cm.leftAt IS NULL")
    Optional<ConversationMemberEntity> findActiveMember(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}