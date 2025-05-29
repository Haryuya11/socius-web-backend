package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.conversation.ConversationRequestDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface ConversationService {

    /**
     * Tạo cuộc trò chuyện nhóm
     *
     * @param groupId   ID của nhóm
     * @param name      Tên cuộc trò chuyện
     * @param creatorId ID của người tạo cuộc trò chuyện
     * @param memberIds Tập hợp các ID của thành viên trong cuộc trò chuyện
     * @return Cuộc trò chuyện đã được tạo
     */
    ConversationResponseDto createGroupConversation(UUID groupId, String name, UUID creatorId, Set<UUID> memberIds);

    /**
     * Xóa cuộc trò chuyện nhóm
     *
     * @param conversationId ID của cuộc trò chuyện cần xóa
     */
    void deleteGroupConversation(UUID conversationId);


    /**
     * Lấy danh sách cuộc trò chuyện của người dùng với phân trang
     *
     * @param userId   ID của người dùng hiện tại
     * @param pageable Thông tin phân trang
     * @return Trang cuộc trò chuyện
     */
    Page<ConversationResponseDto> getUserConversations(UUID userId, Pageable pageable);

    /**
     * Lấy thông tin chi tiết của một cuộc trò chuyện
     *
     * @param userId         ID của người dùng hiện tại
     * @param conversationId ID của cuộc trò chuyện
     * @return Thông tin cuộc trò chuyện
     */
    ConversationResponseDto getConversation(UUID userId, UUID conversationId);

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
     * @param userId1 ID của người dùng thứ nhất
     * @param userId2 ID của người dùng thứ hai
     * @return Cuộc trò chuyện
     */
    ConversationResponseDto getOrCreateDirectConversation(UUID userId1, UUID userId2);

    /**
     * Cập nhật thông tin cuộc trò chuyện
     *
     * @param userId         ID của người dùng thực hiện hành động
     * @param conversationId ID của cuộc trò chuyện
     * @param requestDto     Thông tin cập nhật
     * @return Cuộc trò chuyện đã được cập nhật
     */
    ConversationResponseDto updateConversation(UUID userId, UUID conversationId, ConversationRequestDto requestDto);

}