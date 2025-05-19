package org.socius.sociuswebbackend.services;

import org.socius.sociuswebbackend.model.dtos.conversation.ConversationRequestDto;
import org.socius.sociuswebbackend.model.dtos.conversation.ConversationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ConversationService {

    /**
     * Tạo cuộc trò chuyện mới
     *
     * @param creatorId ID của người tạo
     * @param requestDto Thông tin cuộc trò chuyện
     * @return Cuộc trò chuyện đã được tạo
     */
    ConversationResponseDto createConversation(UUID creatorId, ConversationRequestDto requestDto);

    /**
     * Lấy danh sách cuộc trò chuyện của người dùng với phân trang
     *
     * @param userId ID của người dùng hiện tại
     * @param pageable Thông tin phân trang
     * @return Trang cuộc trò chuyện
     */
    Page<ConversationResponseDto> getUserConversations(UUID userId, Pageable pageable);

    /**
     * Lấy thông tin chi tiết của một cuộc trò chuyện
     *
     * @param userId ID của người dùng hiện tại
     * @param conversationId ID của cuộc trò chuyện
     * @return Thông tin cuộc trò chuyện
     */
    ConversationResponseDto getConversation(UUID userId, UUID conversationId);

    /**
     * Thêm thành viên vào cuộc trò chuyện
     *
     * @param userId ID của người dùng thực hiện hành động
     * @param conversationId ID của cuộc trò chuyện
     * @param memberId ID của người dùng được thêm vào
     * @return Cuộc trò chuyện đã được cập nhật
     */
    ConversationResponseDto addMember(UUID userId, UUID conversationId, UUID memberId);

    /**
     * Xóa thành viên khỏi cuộc trò chuyện
     *
     * @param userId ID của người dùng thực hiện hành động
     * @param conversationId ID của cuộc trò chuyện
     * @param memberId ID của thành viên bị xóa
     * @return Cuộc trò chuyện đã được cập nhật
     */
    ConversationResponseDto removeMember(UUID userId, UUID conversationId, UUID memberId);

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
     * @param userId ID của người dùng thực hiện hành động
     * @param conversationId ID của cuộc trò chuyện
     * @param requestDto Thông tin cập nhật
     * @return Cuộc trò chuyện đã được cập nhật
     */
    ConversationResponseDto updateConversation(UUID userId, UUID conversationId, ConversationRequestDto requestDto);

    /**
     * Rời khỏi cuộc trò chuyện
     *
     * @param userId ID của người dùng muốn rời khỏi
     * @param conversationId ID của cuộc trò chuyện
     * @return true nếu thành công, false nếu không
     */
    boolean leaveConversation(UUID userId, UUID conversationId);
}