package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.conversation.ConversationMemberDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ConversationService {

    /**
     * Tạo cuộc trò chuyện nhóm
     *
     * @param name      Tên cuộc trò chuyện
     * @param creatorId ID của người tạo cuộc trò chuyện
     * @param memberIds Tập hợp các ID của thành viên trong cuộc trò chuyện
     * @return Cuộc trò chuyện đã được tạo
     */
    ConversationResponseDto createGroupConversation(String name, UUID creatorId, Set<UUID> memberIds);

    /**
     * Xóa cuộc trò chuyện nhóm
     *
     * @param conversationId ID của cuộc trò chuyện cần xóa
     */
    void deleteGroupConversation(UUID conversationId);

    /**
     * Lấy danh sách cuộc trò chuyện của người dùng với phân trang
     *
     * @param pageable Thông tin phân trang
     * @return Trang cuộc trò chuyện
     */
    Page<ConversationResponseDto> getUserConversations(Pageable pageable);

    /**
     * Thêm thành viên vào cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param memberId       ID của người dùng được thêm vào
     */
    void addMember(UUID conversationId, UUID memberId);

    /**
     * Thêm nhiều thành viên vào cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param memberIds      Tập hợp các ID của người dùng được thêm vào
     */
    void addMembers(UUID conversationId, Set<UUID> memberIds);

    /**
     * Xóa thành viên khỏi cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param memberId       ID của thành viên bị xóa
     */
    void removeMember(UUID conversationId, UUID memberId);

    /**
     * Xóa nhiều thành viên khỏi cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param memberIds      Tập hợp các ID của thành viên bị xóa
     */
    void removeMembers(UUID conversationId, Set<UUID> memberIds);

    /**
     * Tìm hoặc tạo cuộc trò chuyện trực tiếp giữa hai người dùng
     *
     * @param otherUserId ID của người dùng thứ hai
     * @return Cuộc trò chuyện
     */
    ConversationResponseDto getOrCreateDirectConversation(UUID otherUserId);

    /**
     * Tìm kiếm cuộc trò chuyện theo ID
     *
     * @param conversationId ID của cuộc trò chuyện
     * @return Thông tin cuộc trò chuyện
     */
    ConversationResponseDto findById(UUID conversationId);

    /**
     * Lấy danh sách thành viên trong cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @return Danh sách thành viên
     */
    List<ConversationMemberDto> getConversationMembers(UUID conversationId);

    /**
     * Lấy tất cả cuộc trò chuyện của người dùng
     *
     * @return Danh sách cuộc trò chuyện
     */
    List<ConversationResponseDto> getAllUserConversations();

    /**
     * Cập nhật tên cuộc trò chuyện
     *
     * @param conversationId ID của cuộc trò chuyện
     * @param newName Tên mới
     */
    void updateConversationName(UUID conversationId, String newName);
}